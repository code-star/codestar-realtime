package nl.codestar.consumers.positions

import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import nl.codestar.consumers.positions.PositionsActor.GetLocationsByBox
import nl.codestar.util.BoundingBox
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait PositionsRoutes extends VehicleDisplayInfoJsonSupport with MapVehicleInfoJsonSupport with DefaultJsonProtocol {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  implicit val timeout: Timeout = Timeout(3 seconds) // needed for `?` below

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler

  val positionsActor: ActorRef[PositionsActor.Command]

  lazy val positionsRoutes: Route = {
    pathSingleSlash {
      get {
        complete("Hello!")
      }
    } ~
      path("ns" / "positions") {
        parameters('n.as[Double], 'e.as[Double], 's.as[Double], 'w.as[Double]) { (n, e, s, w) =>
          val future = (positionsActor ? (GetLocationsByBox(BoundingBox(n, e, s, w), _: ActorRef[Seq[VehicleDisplayInfo]])))
            .map(_.toJson.compactPrint)
          onSuccess(future)(complete(_))
        } ~ complete("need to provide the coordinates of a bounding box (n,e,s,w)")
      }
  }

}
