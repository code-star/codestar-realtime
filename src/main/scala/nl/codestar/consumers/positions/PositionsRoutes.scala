package nl.codestar.consumers.positions

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import nl.codestar.consumers.positions.PositionsActor.GetLocationsByBox
import nl.codestar.util.BoundingBox

import scala.concurrent.duration._
import spray.json.JsValue

import scala.concurrent.Await

trait PositionsRoutes {

  implicit val timeout: Timeout = Timeout(3 seconds) // needed for `?` below

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  val positionsActor: ActorRef

  lazy val positionsRoutes: Route = {
    pathSingleSlash {
      get {
        complete("Hello!")
      }
    } ~
      path("ns" / "positions") {
        parameters('n.as[Double], 'e.as[Double], 's.as[Double], 'w.as[Double]) { (n, e, s, w) =>
          val future = (positionsActor ? GetLocationsByBox(BoundingBox(n, e, s, w))).mapTo[JsValue]
          val vehiclePositions = Await.result(future, timeout.duration)
          println(vehiclePositions)
          complete(vehiclePositions.toString)
        } ~ complete("need to provide the coordinates of a bounding box (n,e,s,w)")
      }
  }

}
