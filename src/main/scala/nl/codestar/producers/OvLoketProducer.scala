package nl.codestar.producers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import nl.codestar.data.OVLoketGenerator

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

object OvLoketProducer extends App {
  import GenericProducer._

  implicit val actorSystem: ActorSystem   = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext       = actorSystem.dispatcher

  val topic     = config.getString("feeds.ovloket.ns.topic")
  val port      = config.getInt("feeds.ovloket.ns.port")
  val envelopes = config.getStringList("feeds.ovloket.ns.envelopes").asScala

  val dataSource       = new OVLoketGenerator(port, envelopes)
  val vehiclesProducer = new GenericProducer(topic, dataSource)
  logger.info(s"Connected to TrainLocationProducer($topic, $port, $envelopes)")

  val done = vehiclesProducer.sendPeriodically

  while (true) {
    if (scala.io.StdIn.readChar() == 'q') {
      vehiclesProducer.close()
      Await.ready(done, 10.seconds)
      System.exit(0)
    }
  }

}
