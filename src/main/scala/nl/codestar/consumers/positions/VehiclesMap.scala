package nl.codestar.consumers.positions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}
import nl.codestar.data.{Position, VehicleInfo}
import nl.codestar.util.BoundingBox

case class VehiclesMap(map: Map[String, VehicleInfo] = Map.empty[String, VehicleInfo]) {

  def update(more: Map[String, VehicleInfo]) = new VehiclesMap(map ++ more)

  def garbageCollector(): VehiclesMap = {
    //TODO: remove from map those entries older that x minutes
    new VehiclesMap(map)
  }

  def filterByBoundingBox(box: BoundingBox): VehiclesMap = {
    val m = map.filter { case (_, pos) => box.contains(Position(pos.latitude,pos.longitude)) }
    println(s"#after filter: ${m.values.size}")
    new VehiclesMap(m)
  }

  //  def toJson: JsValue = {
  //    //    val elements =
  //    //      for (
  //    //        (id: String, e: FeedEntity) <- fetch();
  //    //        vehicle: VehiclePosition <- e.getVehicle if e.hasVehicle;
  //    //        position <- vehicle.getPosition if vehicle.hasPosition
  //    //      ) yield (id, position)
  //
  //    val elements = map
  //      .map { case (id, pos) =>
  //        s"""{
  //           |  "type":"node",
  //           |  "id":"$id",
  //           |  "lat":${pos.getLatitude},
  //           |  "lon":${pos.getLongitude},
  //           |  "tags":{
  //           |    "id":"$id"
  //           |  }
  //           |}""".stripMargin
  //      }
  //
  //    val dataString = "{ \"elements\": [\n" + elements.mkString(", ") + "\n] }"
  //    Json.parse(dataString)
  //
  //  }

}

object VehiclesMap extends DefaultJsonProtocol {

//  type t = Map[String, Position]

  def empty: VehiclesMap = new VehiclesMap(Map.empty[String, VehicleInfo])

}

import spray.json.DefaultJsonProtocol

trait VehiclesMapJsonSupport extends SprayJsonSupport {

  // import the default encoders for primitive types (Int, String, Lists, etc)
  import DefaultJsonProtocol._

  implicit val vehiclesMapFormat: JsonFormat[VehiclesMap] = lazyFormat(jsonFormat1(VehiclesMap.apply))
  implicit val vehicleInfoFormat: JsonFormat[VehicleInfo] = lazyFormat(jsonFormat3(VehicleInfo.apply))

}