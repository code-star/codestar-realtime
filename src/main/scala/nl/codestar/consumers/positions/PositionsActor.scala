package nl.codestar.consumers.positions

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import nl.codestar.data.Position
import nl.codestar.util.BoundingBox
import spray.json._
import DefaultJsonProtocol._
import akka.stream.Materializer
import nl.codestar.model.{VehicleInfo, VehicleInfoJsonSupport}

object PositionsActor {
  final case class GetLocationsByBox(box: BoundingBox)
  case class UpdatePosition(id: String, vehicleInfo: VehicleInfo)
  case object Stop

// TODO:
//  final case class GetLocationsByDistance(pos: Position, distance: Double)
//  final case class GetLocationsById(id: String)

  def props: Props = Props[PositionsActor]
}

/**
  * Keeps track of latest vehicle info, processes updates and allows querying by bounding box
  */
class PositionsActor(topic: String, groupId: String)(implicit actorSystem: ActorSystem, materializer: Materializer)
    extends Actor
    with ActorLogging
    with VehicleDisplayInfoJsonSupport {
  import PositionsActor._

  private val consumer = new PositionsConsumer(topic, receiver = self)

  /**
    * Cache of latest info for each id.
    */
  private var lastInfoCache = Map.empty[String, VehicleInfo]

  def receive: Receive = {
    case UpdatePosition(id, vehicleInfo) =>
      lastInfoCache = lastInfoCache + (id -> vehicleInfo)

    case GetLocationsByBox(box) =>
      val positionsInBox = lastInfoCache
        .filter { case (_, info) => box.contains(Position(info.latitude, info.longitude)) }
        .map { case (k, v) => VehicleDisplayInfo.fromInfo(k, v) }
        .toSeq
      sender ! positionsInBox.toJson

    case Stop =>
      consumer.close() // TODO await and log
  }
}

// not used for the moment
trait MapVehicleInfoJsonSupport extends VehicleInfoJsonSupport {
  import spray.json._

  implicit def VehiclesMapJsonFormat: JsonFormat[Map[String, VehicleInfo]] =
    new RootJsonFormat[Map[String, VehicleInfo]] {
      def write(m: Map[String, VehicleInfo]): JsValue = JsObject(m.map { case (k, v) => k -> v.toJson }.toSeq: _*)
      def read(value: JsValue): Map[String, VehicleInfo] =
        value.asJsObject.fields.map { case (k, v) => k -> v.convertTo[VehicleInfo] }
    }
}
