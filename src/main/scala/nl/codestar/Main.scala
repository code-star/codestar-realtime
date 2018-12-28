package nl.codestar

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import nl.codestar.consumers.positions.{PositionsActor, PositionsConsumer, PositionsRoutes}
import nl.codestar.feeds.VehicleInfoProducer
import nl.codestar.feeds.openov.OpenOVSource
import nl.codestar.feeds.ovloket.OVLoketSource
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Main extends App with PositionsRoutes {

  implicit def system: ActorSystem                = ActorSystem("positionsAkkaHttpServer")
  implicit val materializer: ActorMaterializer    = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher // needed for the future map/flatmap

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val config: Config = ConfigFactory.load()

  val topic   = config.getString("app.kafka.topics.vehicleInfo")
  val groupId = config.getString("app.kafka.consumerGroupId")

  // Start feeds
  val ovLoketSource = createOvLoketFeeder
  val openOvSource  = createOpenOvFeeder
  val sources       = ovLoketSource.source merge openOvSource.source
  val kafkaProducer = new VehicleInfoProducer(topic, sources)

  // Start consumer
  val positionsActor: typed.ActorRef[PositionsActor.Command] = system.spawn(PositionsActor.behavior(), "positionsActor")
  val positionsConsumer                                      = new PositionsConsumer(topic, groupId, positionsActor)

  // Start web server
  lazy val routes: Route = positionsRoutes // from the PositionsRoutes trait

  private val interface = config.getString("webserver.positions.interface")
  private val port      = config.getInt("webserver.positions.port")
  val bindingFuture     = Http().bindAndHandle(routes, interface, port)

  logger.info(s"Location server online at http://$interface:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete { _ =>
      positionsConsumer.close()
      kafkaProducer.close()
      system.terminate()
    } // and shutdown when done

  private def createOpenOvFeeder = {
    val url = config.getString("feeds.openov.vehiclePositions.url")
    new OpenOVSource(url)
  }

  private def createOvLoketFeeder = {
    val port      = config.getInt("feeds.ovloket.ns.port")
    val envelopes = config.getStringList("feeds.ovloket.ns.envelopes").asScala

    new OVLoketSource(port, envelopes)
  }
}
