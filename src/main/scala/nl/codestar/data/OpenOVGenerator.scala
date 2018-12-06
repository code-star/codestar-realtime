package nl.codestar.data

import java.net.URL

import scala.collection.JavaConverters._
import com.google.transit.realtime.GtfsRealtime
import com.google.transit.realtime.GtfsRealtime.FeedMessage

import scala.collection.mutable

class OpenOVGenerator(feederUrl: String) extends DataSourceGenerator {

  private val url = new URL(feederUrl)
  protected val feed: FeedMessage = FeedMessage.parseFrom(url.openStream)
  protected val data: mutable.Buffer[GtfsRealtime.FeedEntity] = feed.getEntityList.asScala

  //TODO: use feed validator: https://github.com/google/transitfeed/wiki/FeedValidator

  def poll(): Map[String, Array[Byte]] = {
    //    for (
    //      entity : FeedEntity <- data if entity.hasVehicle;
    //      id : String <- entity.getIdBytes if !busCompanies.contains(id.split(":")(1))
    //    ) yield id
    data
      .filter(_.hasVehicle)
      .map(entity => (entity.getId, entity.toByteArray))
      .toMap
  }

}
