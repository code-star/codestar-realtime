package nl.codestar.model

import com.google.transit.realtime.GtfsRealtime
import spray.json.DefaultJsonProtocol

case class Position(latitude: Double, longitude: Double) {

  override def toString: String = s"($latitude,$longitude)"

}

object Position extends DefaultJsonProtocol {

  val utrechtCentraal = Position(52.0894444, 5.1077981)

  implicit def gtfsPositionToPosition(p: GtfsRealtime.Position): Position = Position(p.getLatitude, p.getLongitude)

}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait PositionJsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val positionFormat: JsonFormat[Position] = lazyFormat(jsonFormat2(Position.apply))

}
