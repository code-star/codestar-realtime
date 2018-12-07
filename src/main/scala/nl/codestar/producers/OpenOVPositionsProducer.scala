package nl.codestar.producers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import nl.codestar.data.OpenOVGenerator

import scala.concurrent.ExecutionContext

object OpenOVPositionsProducer extends App {
  import GenericProducer._

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val openovFeedUrl = config.getString("feeds.openov.vehiclePositions.url")
  val topic = config.getString("feeds.openov.vehiclePositions.topic")

  val dataSource = new OpenOVGenerator(openovFeedUrl)
  val vehiclesProducer = new GenericProducer(topic, dataSource)
  logger.info(s"Connected to OpenOVPositionsProducer($topic, $openovFeedUrl)")

  vehiclesProducer.sendPeriodically(60 * 1000)
  vehiclesProducer.close()

}