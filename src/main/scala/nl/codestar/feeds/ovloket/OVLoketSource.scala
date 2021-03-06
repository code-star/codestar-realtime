package nl.codestar.feeds.ovloket

/**
  *
  * BISON KV6, KV15, KV17:                 tcp://pubsub.besteffort.ndovloket.nl:7658
  * KV78Turbo:                             tcp://pubsub.besteffort.ndovloket.nl:7817
  * NS InfoPlus DVS-PPV:                   tcp://pubsub.besteffort.ndovloket.nl:7664
  * NS AR-NU                               tcp://pubsub.besteffort.ndovloket.nl:7662
  *
  * For more details about the different data streams see: http://data.ndovloket.nl/REALTIME.TXT
  */
/**
  * KV78Turbo documentation: http://data.ndovloket.nl/docs/kv78turbo/Documentatie%20KV78%20turbo-0.5.pdf
  */
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.zip.GZIPInputStream

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.LazyLogging
import nl.codestar.feeds.{VehicleInfoSource, ZeroMqSource}
import nl.codestar.model.VehicleInfo

import scala.concurrent.{ExecutionContext, Future}

class OVLoketSource(port: Int, envelopes: Iterable[String], url: String = "pubsub.besteffort.ndovloket.nl")(implicit ec: ExecutionContext, actorSystem: ActorSystem)
    extends VehicleInfoSource
    with LazyLogging {

  def source: Source[(String, VehicleInfo), Future[Done]] =
    ZeroMqSource(url, port, envelopes.toSeq)
      .mapConcat { msg =>
        val msgType    = msg.pop.toString
        val rawContent = msg.pop.getData
        val content    = unzip(rawContent)
        val xml        = scala.xml.XML.loadString(content)

        val vehicleInfos = TrainLocations
          .fromXMl(xml)
          .locations
          .map(l => (l.number.toString, l.parts))
          .toMap
          .mapValues(_.head) // Some TrainLocation have more than one MaterialPart; take only the first one.
          .mapValues(p => VehicleInfo(p.latitude, p.longitude, Instant.ofEpochMilli(VehicleInfo.gpsTimeToMillis(p.gpsDatetime))))

        vehicleInfos
      }
      .watchTermination() {
        case (stop, done) =>
          done.map { _ =>
            logger.info("Closing ZeroMQ source")
            stop()
            Done
          }
      }

  private def unzip(xs: Array[Byte]): String = {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(xs))
    scala.io.Source.fromInputStream(inputStream).mkString
  }
}
