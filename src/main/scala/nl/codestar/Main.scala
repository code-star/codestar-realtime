package nl.codestar

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import nl.codestar.consumers.positions.{PositionsActor, PositionsConsumer, PositionsRoutes}
import nl.codestar.feeds.ovloket.OVLoketGenerator
import nl.codestar.producers.GenericProducer
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

  private val topic   = Main.config.getString("feeds.ovloket.ns.topic")
  private val groupId = Main.config.getString("feeds.ovloket.ns.groupId")

  val positionsActor: typed.ActorRef[PositionsActor.Command] = system.spawn(PositionsActor.behavior(), "positionsActor")
  val positionsConsumer                                      = new PositionsConsumer(topic, groupId, positionsActor)

  lazy val routes: Route = positionsRoutes // from the PositionsRoutes trait

  private val interface = config.getString("webserver.positions.interface")
  private val port      = config.getInt("webserver.positions.port")
  val bindingFuture     = Http().bindAndHandle(routes, interface, port)

  val (done, vehiclesProducer) = {
    val topic     = config.getString("feeds.ovloket.ns.topic")
    val port      = config.getInt("feeds.ovloket.ns.port")
    val envelopes = config.getStringList("feeds.ovloket.ns.envelopes").asScala

    val dataSource       = new OVLoketGenerator(port, envelopes)
    val vehiclesProducer = new GenericProducer(topic, dataSource)
    logger.info(s"Connected to TrainLocationProducer($topic, $port, $envelopes)")

    val done = vehiclesProducer.sendPeriodically

    (done, vehiclesProducer)
  }

  logger.info(s"Location server online at http://$interface:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .flatMap { _ =>
      vehiclesProducer.close()
      done
    }
    .onComplete { _ =>
      system.terminate()
    } // and shutdown when done

}
