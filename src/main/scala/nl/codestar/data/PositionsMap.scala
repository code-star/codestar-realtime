package nl.codestar.data

import com.google.transit.realtime.GtfsRealtime.Position
import nl.codestar.util.BoundingBox

class PositionsMap(protected val map: Map[String, Position]) {

  def onlyInBoundingBox(box: BoundingBox): PositionsMap = {
    val m = map.filter { case (_, pos) => box.contains(pos) }
    println(s"#after filter: ${m.values.size}")
    new PositionsMap(m)
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

  def update(more: Map[String, Position]) = new PositionsMap(map ++ more)
}

object PositionsMap {

  def empty: PositionsMap = new PositionsMap(Map.empty[String, Position])

}
