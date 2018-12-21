package nl.codestar.producers

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.{KillSwitches, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import nl.codestar.data.DataSourceGenerator
import nl.codestar.model.{VehicleInfo, VehicleInfoJsonSupport}
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GenericProducer(topic: String, source: DataSourceGenerator)(implicit actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext)
    extends VehicleInfoJsonSupport {

  import GenericProducer._
  import nl.codestar.util.KafkaUtil._

  val producerSettings = kafkaProducerSettings[String, VehicleInfo]
    .withBootstrapServers(brokers)

  val killSwitch = KillSwitches.shared("close")

  def sendPeriodically: Future[Done] = {
    val done = source.source
      .map { case (id, content) => new ProducerRecord(topic, id, content) }
      .via(killSwitch.flow)
      .runWith(Producer.plainSink(producerSettings))

    done.onComplete {
      case Success(_) => logger.info(s"Producer ${topic} completed successfully")
      case Failure(e) => logger.error(s"Producer ${topic} completed with error", e)
    }

    done
  }

  def close(): Unit = {
    println("Shutting down")
    killSwitch.shutdown()
  }
}

object GenericProducer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val config: Config = ConfigFactory.load()

  val brokers: String = config.getString("kafka.brokers")

}
