package nl.codestar.consumers.positions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import nl.codestar.data.Position
import nl.codestar.model.{VehicleInfo, VehicleInfoJsonSupport}
import nl.codestar.util.BoundingBox

import scala.collection.mutable

class VehiclesMap {

  type t = mutable.Map[String, VehicleInfo]

  val map: t = mutable.Map.empty

  def update(more: Map[String, VehicleInfo]): Unit = map ++= more

  def garbageCollector(): t = {
    //TODO: remove from map those entries older that x minutes
    map
  }

  def filterByBoundingBox(box: BoundingBox): t = {
    val m = map.filter { case (_, info) => box.contains(Position(info.latitude, info.longitude)) }
    println(s"#after filter: ${m.values.size}")
    m
  }

  override def toString: String = map.toString

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

trait VehiclesMapJsonSupport extends SprayJsonSupport with VehicleInfoJsonSupport {
  import spray.json._

  implicit def VehiclesMapJsonFormat: JsonFormat[VehiclesMap] = new RootJsonFormat[VehiclesMap] {

    def write(m: VehiclesMap): JsValue = JsObject(m.map.map { case (k, v) => k -> v.toJson }.toSeq: _*)

    def read(value: JsValue): VehiclesMap = {
      val m  = value.asJsObject.fields.map { case (k, v) => k -> v.convertTo[VehicleInfo] }
      val vm = new VehiclesMap()
      vm.update(m)
      vm
    }

  }

}
