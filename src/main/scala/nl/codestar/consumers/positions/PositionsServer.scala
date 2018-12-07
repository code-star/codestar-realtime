package nl.codestar.consumers.positions

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object PositionsServer extends App with PositionsRoutes {

  implicit val system: ActorSystem = ActorSystem("positionsAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher // needed for the future map/flatmap

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val config: Config = ConfigFactory.load()

  private val topic = PositionsServer.config.getString("feeds.ovloket.ns.topic")
  private val groupId = PositionsServer.config.getString("feeds.ovloket.ns.groupId")

  val positionsActor: ActorRef = system.actorOf(Props(new PositionsActor(topic, groupId)), "positionsActor")

  lazy val routes: Route = positionsRoutes // from the PositionsRoutes trait

  private val interface = config.getString("webserver.positions.interface")
  private val port = config.getInt("webserver.positions.port")
  val bindingFuture = Http().bindAndHandle(routes, interface, port)
  logger.info(s"Location server online at http://$interface:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
