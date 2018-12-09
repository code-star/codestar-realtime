package nl.codestar.model

import java.time.Instant

import spray.json.{DefaultJsonProtocol, JsNumber, JsValue}

case class VehicleInfo(latitude: Double, longitude: Double, time: Instant) extends Ordered[VehicleInfo] {

  override def compare(that: VehicleInfo): Int = this.time compareTo that.time

}

object VehicleInfo extends DefaultJsonProtocol {

  /**
    * Converts time formats.
    *
    * @param str GPS date time taken from OVloket
    * @return Unix Timestamp
    */
  def gpsTimeToMillis(str: String): Long = {
    val desiredTime = str.replace('T', ' ').replace('Z', '\0')
    val format      = new java.text.SimpleDateFormat("yyyy-mm-dd HH:m:ss")
    format.parse(desiredTime).getTime
  }

}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait VehicleInfoJsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._
  import spray.json._

  implicit val instantFormat: JsonFormat[Instant] = new JsonFormat[Instant] {
    override def write(obj: Instant): JsValue = JsNumber(obj.toEpochMilli)

    override def read(json: JsValue): Instant = json match {
      case JsNumber(value) => Instant.ofEpochMilli(value.toLong)
      case _               => throw new IllegalArgumentException(s"Unknown instant json: ${json}")
    }
  }

  implicit val positionFormat: JsonFormat[VehicleInfo] = lazyFormat(jsonFormat3(VehicleInfo.apply))

}
