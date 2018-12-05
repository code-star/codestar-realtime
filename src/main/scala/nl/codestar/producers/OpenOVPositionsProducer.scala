package nl.codestar.producers

import nl.codestar.data.{ DataSource, OpenOVReader }

class OpenOVPositionsProducer(topic: String, dataSource: DataSource)
  extends GenericProducer(topic, dataSource)

object OpenOVPositionsProducer extends App {
  import GenericProducer._

  val openovFeedUrl = config.getString("feeds.openov.vehiclePositions.url")
  val topic = config.getString("feeds.openov.vehiclePositions.topic")

  val dataSource = new OpenOVReader(openovFeedUrl)
  val vehiclesProducer = new OpenOVPositionsProducer(topic, dataSource)
  logger.info(s"Connected to OpenOVPositionsProducer($topic, $openovFeedUrl)")

  vehiclesProducer.sendPeriodically(60 * 1000)
  vehiclesProducer.close()

}