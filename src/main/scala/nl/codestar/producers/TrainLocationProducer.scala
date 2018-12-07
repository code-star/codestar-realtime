package nl.codestar.producers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import nl.codestar.data.OVLoketGenerator

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext


object TrainLocationProducer extends App {
  import GenericProducer._

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val topic = config.getString("feeds.ovloket.ns.topic")
  val port = config.getInt("feeds.ovloket.ns.port")
  val envelopes = config.getStringList("feeds.ovloket.ns.envelopes").asScala

  val dataSource = new OVLoketGenerator(port, envelopes)
  val vehiclesProducer = new GenericProducer(topic, dataSource)
  logger.info(s"Connected to TrainLocationProducer($topic, $port, $envelopes)")

  vehiclesProducer.sendPeriodically(1000)
  vehiclesProducer.close()
}
