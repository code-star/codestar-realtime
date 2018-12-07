package nl.codestar.producers

import collection.JavaConverters._
import nl.codestar.data.{ DataSourceGenerator, OVLoketGenerator }

class TrainLocationProducer(topic: String, dataSource: DataSourceGenerator)
  extends GenericProducer(topic, dataSource)

object TrainLocationProducer extends App {
  import GenericProducer._

  val topic = config.getString("feeds.ovloket.ns.topic")
  val port = config.getInt("feeds.ovloket.ns.port")
  val envelopes = config.getStringList("feeds.ovloket.ns.envelopes").asScala

  val dataSource = new OVLoketGenerator(port, envelopes)
  val vehiclesProducer = new TrainLocationProducer(topic, dataSource)
  logger.info(s"Connected to TrainLocationProducer($topic, $port, $envelopes)")

  vehiclesProducer.sendPeriodically(1000)
  vehiclesProducer.close()
}
