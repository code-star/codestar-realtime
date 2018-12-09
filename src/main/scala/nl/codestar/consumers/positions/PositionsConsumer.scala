package nl.codestar.consumers.positions

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import nl.codestar.consumers.positions.PositionsActor.UpdatePosition
import nl.codestar.model.{VehicleInfo, VehicleInfoJsonSupport}
import nl.codestar.producers.GenericProducer

import scala.concurrent.{ExecutionContext, Future}

/**
  * Consumes VehicleInfo from the topic and pushes them to the given receiver actor
  */
class PositionsConsumer(topic: String, receiver: ActorRef)(implicit actorSystem: ActorSystem,
                                                           materializer: Materializer)
    extends VehicleInfoJsonSupport {

  import nl.codestar.util.KafkaUtil._

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val consumerSettings = kafkaConsumerSettings[String, VehicleInfo]
    .withBootstrapServers(GenericProducer.brokers)

  val stream = Consumer
    .committableSource(consumerSettings, Subscriptions.topics(topic))
    .alsoToMat(Sink.foreach { msg =>
      receiver ! UpdatePosition(msg.record.key(), msg.record.value)
    })(Keep.both)
    .toMat(Committer.sink(CommitterSettings(actorSystem)).contramap(_.committableOffset))(Keep.both)
    .mapMaterializedValue(flattenTuple)

  val (control, done1, committerSinkDone) = stream.run()

  def close(): Future[Done] =
    for {
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
