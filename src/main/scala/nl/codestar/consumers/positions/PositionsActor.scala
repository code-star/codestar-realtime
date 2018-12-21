package nl.codestar.consumers.positions

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import nl.codestar.data.Position
import nl.codestar.model.{VehicleInfo, VehicleInfoJsonSupport}
import nl.codestar.util.BoundingBox

object PositionsActor {
  sealed trait Command
  final case class GetLocationsByBox(box: BoundingBox, replyTo: ActorRef[Seq[VehicleDisplayInfo]]) extends Command
  case class UpdatePosition(id: String, vehicleInfo: VehicleInfo)                                  extends Command
  case object Stop                                                                                 extends Command

  // TODO:
  //  final case class GetLocationsByDistance(pos: Position, distance: Double)
  //  final case class GetLocationsById(id: String)

  def behavior(lastInfoCache: Map[String, VehicleInfo] = Map.empty): Behavior[Command] = Behaviors.receiveMessage {
    case UpdatePosition(id, vehicleInfo) =>
      behavior(lastInfoCache + (id -> vehicleInfo))

    case GetLocationsByBox(box, replyTo) =>
      val positionsInBox = lastInfoCache
        .filter { case (_, info) => box.contains(Position(info.latitude, info.longitude)) }
        .map { case (k, v) => VehicleDisplayInfo.fromInfo(k, v) }
        .toSeq
      replyTo ! positionsInBox
      Behaviors.same

    case Stop =>
      Behaviors.stopped
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
