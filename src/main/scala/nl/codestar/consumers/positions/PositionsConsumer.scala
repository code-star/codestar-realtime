package nl.codestar.consumers.positions

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
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
class PositionsConsumer(topic: String, groupId: String, receiver: ActorRef[UpdatePosition])(implicit actorSystem: ActorSystem, materializer: Materializer)
    extends VehicleInfoJsonSupport {

  import nl.codestar.util.KafkaUtil._

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val source = Consumer
    .committableSource(kafkaConsumerSettings[String, VehicleInfo]
                         .withBootstrapServers(GenericProducer.brokers)
                         .withGroupId(groupId),
                       Subscriptions.topics(topic))

  val stream = source
    .alsoToMat(Sink.foreach { msg =>
      println(s"Consuming message ${msg}")

      receiver ! UpdatePosition(msg.record.key(), msg.record.value)
    })(Keep.both)
    .toMat(Committer.sink(CommitterSettings(actorSystem)).contramap(_.committableOffset))(Keep.both)
    .mapMaterializedValue(flattenTuple3)

  val (control, done1, committerSinkDone) = stream.run()

  def close(): Future[Done] =
    for {
      _ <- control.shutdown()
      _ <- done1
      _ <- committerSinkDone
    } yield Done

}
