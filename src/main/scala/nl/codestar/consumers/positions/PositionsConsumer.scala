package nl.codestar.consumers.positions

import java.util

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import nl.codestar.consumers.positions.PositionsActor.UpdatePosition
import nl.codestar.data.{VehicleInfo, VehicleInfoJsonSupport}
import nl.codestar.producers.GenericProducer
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}
import spray.json.{JsonFormat, JsonParser}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Consumes VehicleInfo from the topic and pushes them to the given receiver actor
  */
class PositionsConsumer(topic: String, receiver: ActorRef)(implicit actorSystem: ActorSystem, materializer: Materializer) extends VehicleInfoJsonSupport {
  import PositionsConsumer._

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val consumerSettings = ConsumerSettings(
    system = actorSystem,
    keyDeserializer = new StringDeserializer,
    valueDeserializer = jsonDeserializer[VehicleInfo])
    .withBootstrapServers(GenericProducer.brokers)

  val stream = Consumer.committableSource(consumerSettings, Subscriptions.topics(topic))
      .alsoToMat(Sink.foreach { msg =>
        receiver ! UpdatePosition(msg.record.key(), msg.record.value)
      })(Keep.both)
    .toMat(Committer.sink(CommitterSettings(actorSystem)).contramap(_.committableOffset))(Keep.both)
    .mapMaterializedValue(flattenTuple)

  val (control, done1, committerSinkDone) = stream.run()

  def close(): Future[Done] = for {
    _ <- control.shutdown()
    _ <- done1
    _ <- committerSinkDone
  } yield Done

//  /**
//    * Data from OpenOV
//    */
//  def pollGTFS(): Map[String, GtfsRealtime.Position] = this.synchronized {
//    val records = consumer.poll(3 * 1000)
//
//    val entities: Map[String, FeedEntity] = records.asScala
//      .groupBy(_.key)
//      .mapValues(_.last.value)
//      .mapValues(FeedEntity.parseFrom)
//
//    entities
//      .filter { case (_, e) => e.hasVehicle }
//      .mapValues(_.getVehicle)
//      .filter { case (_, vehicle) => vehicle.hasPosition }
//      .mapValues(_.getPosition)
//
//  }


}

object PositionsConsumer {
  def jsonDeserializer[T: JsonFormat]: Deserializer[T] = new Deserializer[T] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def close(): Unit = ()

    override def deserialize(topic: String, data: Array[Byte]): T = JsonParser(data).convertTo[T]
  }

  def flattenTuple[A, B, C]: (((A, B), C)) => (A, B, C) = {
    case ((a, b), c) => (a, b, c)
  }
}