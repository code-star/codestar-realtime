package nl.codestar.consumers.positions

import akka.actor.{ Actor, ActorLogging, Props }
import nl.codestar.data.{ Position, VehicleInfo, VehicleInfoJsonSupport }
import nl.codestar.util.BoundingBox
import spray.json._
import DefaultJsonProtocol._

object PositionsActor {
  final case class GetLocationsByBox(box: BoundingBox)

  // TODO:
  //  final case class GetLocationsByDistance(pos: Position, distance: Double)
  //  final case class GetLocationsById(id: String)

  def props: Props = Props[PositionsActor]
}

class PositionsActor(topic: String, groupId: String)
  extends Actor with ActorLogging
    with VehicleDisplayInfoJsonSupport {
  import PositionsActor._

  private val consumer = new PositionsConsumer(topic, groupId)

  /**
   * Cache of latest info for each id.
   */
  private var lastInfoCache = Map.empty[String, VehicleInfo]

  def receive: Receive = {
    case GetLocationsByBox(box) =>
      val newData = consumer.poll()
      lastInfoCache = lastInfoCache ++ newData
      val positionsInBox = lastInfoCache
        .filter { case (_, info) => box.contains(Position(info.latitude, info.longitude)) }
        .map { case (k, v) => VehicleDisplayInfo.fromInfo(k, v) }.toSeq
      sender ! positionsInBox.toJson
  }
}

// not used for the moment
trait MapVehicleInfoJsonSupport extends VehicleInfoJsonSupport {
  import spray.json._

  implicit def VehiclesMapJsonFormat: JsonFormat[Map[String, VehicleInfo]] = new RootJsonFormat[Map[String, VehicleInfo]] {
    def write(m: Map[String, VehicleInfo]): JsValue = JsObject(m.map { case (k, v) => k -> v.toJson }.toSeq: _*)
    def read(value: JsValue): Map[String, VehicleInfo] =
      value.asJsObject.fields.map { case (k, v) => k -> v.convertTo[VehicleInfo] }
  }
}