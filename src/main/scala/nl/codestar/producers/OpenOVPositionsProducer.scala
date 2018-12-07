package nl.codestar.producers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import nl.codestar.data.{DataSourceGenerator, OpenOVGenerator}

class OpenOVPositionsProducer(topic: String, dataSource: DataSourceGenerator)(implicit materializer: Materializer)
  extends GenericProducer(topic, dataSource)

object OpenOVPositionsProducer extends App {
  import GenericProducer._

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  val openovFeedUrl = config.getString("feeds.openov.vehiclePositions.url")
  val topic = config.getString("feeds.openov.vehiclePositions.topic")

  val dataSource = new OpenOVGenerator(openovFeedUrl)
  val vehiclesProducer = new OpenOVPositionsProducer(topic, dataSource)
  logger.info(s"Connected to OpenOVPositionsProducer($topic, $openovFeedUrl)")

  vehiclesProducer.sendPeriodically(60 * 1000)
  vehiclesProducer.close()

}