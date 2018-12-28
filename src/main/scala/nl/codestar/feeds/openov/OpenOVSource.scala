package nl.codestar.feeds.openov

import java.io.InputStream
import java.net.URL
import java.time.Instant

import akka.Done
import akka.stream.scaladsl.{Keep, Source}
import com.google.transit.realtime.GtfsRealtime.{FeedEntity, FeedMessage}
import com.typesafe.scalalogging.LazyLogging
import nl.codestar.feeds.VehicleInfoSource
import nl.codestar.model.VehicleInfo

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

class OpenOVSource(feederUrl: String, pollInterval: FiniteDuration = 10.seconds) extends VehicleInfoSource with LazyLogging {

  private val url = new URL(feederUrl)

  //TODO: use feed validator: https://github.com/google/transitfeed/wiki/FeedValidator

  override def source: Source[(String, VehicleInfo), Future[Done]] = {
    Source
      .tick(0.seconds, pollInterval, ())
      .flatMapConcat { _ =>
        val feed = FeedMessage.parseFrom(url.openStream)
        val data = feed.getEntityList.asScala

        //    for (
        //      entity : FeedEntity <- data if entity.hasVehicle;
        //      id : String <- entity.getIdBytes if !busCompanies.contains(id.split(":")(1))
        //    ) yield id
        val d = data
          .filter(_.hasVehicle)
          .filter(_.getVehicle.hasPosition)
          .map(e => (e.getId, vehicleInfoFromFeedEntity(e)))

        logger.info(s"Received ${d.size} records")

        Source.fromIterator(() => d.toIterator)
      }
      .watchTermination()(Keep.right)
  }

  val vehicleInfoFromFeedEntity: FeedEntity => VehicleInfo =
    e =>
      VehicleInfo(
        latitude = e.getVehicle.getPosition.getLatitude,
        longitude = e.getVehicle.getPosition.getLongitude,
        time = Instant.ofEpochSecond(e.getVehicle.getTimestamp)
    )
}
