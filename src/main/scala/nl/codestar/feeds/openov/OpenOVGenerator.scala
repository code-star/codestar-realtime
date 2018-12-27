package nl.codestar.feeds.openov

import java.io.InputStream
import java.net.URL
import java.time.Instant

import akka.Done
import akka.stream.scaladsl.{Keep, Source}
import com.google.transit.realtime.GtfsRealtime.{FeedEntity, FeedMessage}
import nl.codestar.feeds.DataSourceGenerator
import nl.codestar.model.VehicleInfo

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

class OpenOVGenerator(feederUrl: String) extends DataSourceGenerator {

  private val url                 = new URL(feederUrl)
  private val stream: InputStream = url.openStream

  //TODO: use feed validator: https://github.com/google/transitfeed/wiki/FeedValidator

  override def source: Source[(String, VehicleInfo), Future[Done]] = {
    Source
      .tick(0.seconds, 1.second, ())
      .flatMapConcat { _ =>
        val feed = FeedMessage.parseFrom(stream)
        val data = feed.getEntityList.asScala

        //    for (
        //      entity : FeedEntity <- data if entity.hasVehicle;
        //      id : String <- entity.getIdBytes if !busCompanies.contains(id.split(":")(1))
        //    ) yield id
        val d = data
          .filter(_.hasVehicle)
          .map(e => (e.getId, vehicleInfoFromFeedEntity(e)))

        Source.fromIterator(() => d.toIterator)
      }
      .watchTermination()(Keep.right)
  }

  val vehicleInfoFromFeedEntity: FeedEntity => VehicleInfo =
    e => VehicleInfo(latitude = e.getVehicle.getPosition.getLatitude, longitude = e.getVehicle.getPosition.getLongitude, time = Instant.ofEpochSecond(e.getVehicle.getTimestamp))
}
