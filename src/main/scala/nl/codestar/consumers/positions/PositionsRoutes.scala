package nl.codestar.consumers.positions

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import nl.codestar.consumers.positions.PositionsActor.GetByBox
import nl.codestar.util.BoundingBox

import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.ToResponseMarshallable

trait PositionsRoutes {

  implicit val timeout: Timeout = Timeout(5 seconds) // needed for `?` below

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  def positionsActor: ActorRef

  lazy val positionsRoutes: Route = {
    pathSingleSlash {
      get {
        complete("Hello!")
      }
    } ~
    path("ns" / "positions") {
      parameters('n.as[Double], 'e.as[Double], 's.as[Double], 'w.as[Double]) { (n, e, s, w) =>
        val vehiclePositions = (positionsActor ? GetByBox(BoundingBox(n,e,s,w))).mapTo[ToResponseMarshallable]
        complete(vehiclePositions)
      } ~ complete("need to provide the coordinates of a bounding box (n,e,s,w)")
    }
  }

}
