package nl.codestar.consumers.positions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import nl.codestar.data.VehicleInfo
import spray.json.{ DefaultJsonProtocol, JsonFormat }

case class VehicleDisplayInfoTags(
  id: String,
  time: Long,
  text: String
)

/**
 * Information about a vehicle to be displayed on the map.
 * @param `type` always "node"
 * @param tags data to be displayed when clicked in the node.
 */
case class VehicleDisplayInfo(
  `type`: String = "node",
  id: String,
  latitude: Double,
  longitude: Double,
  tags: VehicleDisplayInfoTags
)

object VehicleDisplayInfo {

  def fromInfo(id: String, i: VehicleInfo) =
    VehicleDisplayInfo("node", id, i.latitude, i.longitude, VehicleDisplayInfoTags(id, i.time, ""))

}

trait VehicleDisplayInfoJsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val vehicleDisplayInfoFormat: JsonFormat[VehicleDisplayInfo] = lazyFormat(jsonFormat5(VehicleDisplayInfo.apply))
  implicit val vehicleDisplayInfoTagsFormat: JsonFormat[VehicleDisplayInfoTags] = lazyFormat(jsonFormat3(VehicleDisplayInfoTags.apply))

}
