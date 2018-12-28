package nl.codestar.consumers.positions

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.typesafe.scalalogging.Logger
import nl.codestar.model.{Position, VehicleInfo, VehicleInfoJsonSupport}
import nl.codestar.util.BoundingBox
import nl.codestar.consumers.positions.PositionsActor._

/**
  * Actor that receives updates of vehicle info and stores them in a cache for retrieval
  *
  * Forward the actor [[UpdatePosition]] messages or send it [[GetLocationsByBox]] to retrieve the latest positions
  */
object PositionsActor {
  val logger = Logger(getClass)

  def behavior(lastInfoCache: Map[String, VehicleInfo] = Map.empty): Behavior[Command] = Behaviors.receiveMessage {
    case UpdatePosition(id, vehicleInfo) =>
      val newCache = if (lastInfoCache.get(id).forall(_.time isBefore vehicleInfo.time)) {
        logger.info(s"Processing update for vehicle ${id}")
        lastInfoCache + (id -> vehicleInfo)
      } else {
        // Ignore obsolete updates
        logger.info(s"Ignoring obsolete update for vehicle ${id}")
        lastInfoCache
      }

      behavior(newCache)

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

  sealed trait Command
  final case class GetLocationsByBox(box: BoundingBox, replyTo: ActorRef[Seq[VehicleDisplayInfo]]) extends Command
  case class UpdatePosition(id: String, vehicleInfo: VehicleInfo)                                  extends Command
  case object Stop                                                                                 extends Command

  // TODO:
  //  final case class GetLocationsByDistance(pos: Position, distance: Double)
  //  final case class GetLocationsById(id: String)
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
