package nl.codestar.data

import spray.json.DefaultJsonProtocol

case class VehicleInfo
  (latitude: Double,
   longitude: Double,
   time: Long
  ) extends Ordered[VehicleInfo] {

  override def compare(that: VehicleInfo): Int = this.time compare that.time

}

object VehicleInfo extends DefaultJsonProtocol {

  /**
    * Converts time formats.
    * @param str GPS date time taken from OVloket
    * @return Unix Timestamp
    */
  def gpsTimeToMillis(str: String): Long = {
    val desiredTime = str.replace('T',' ').replace('Z','\0')
    val format = new java.text.SimpleDateFormat("yyyy-mm-dd HH:m:ss")
    format.parse(desiredTime).getTime
  }

}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, JsonFormat }

trait VehicleInfoJsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val positionFormat: JsonFormat[VehicleInfo] = lazyFormat(jsonFormat3(VehicleInfo.apply))

}