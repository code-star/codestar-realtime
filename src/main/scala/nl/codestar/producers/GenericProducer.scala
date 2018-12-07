package nl.codestar.producers

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{KillSwitches, Materializer}
import akka.stream.scaladsl.Source
import com.typesafe.config.{Config, ConfigFactory}
import nl.codestar.data.DataSourceGenerator
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class GenericProducer(topic: String, source: DataSourceGenerator)(implicit materializer: Materializer, ec: ExecutionContext) {

  import GenericProducer._

  //  val max_request_size: String = (5 * 1024 * 1024).toString

  val producerSettings = ProducerSettings(config, new StringSerializer, new ByteArraySerializer)
    .withBootstrapServers(brokers)

  val killSwitch = KillSwitches.shared("close")

  def sendPeriodically(intervalMillis: Int = 1000): Unit = {
    val done = Source.tick(0.seconds, intervalMillis.millis, ())
      .flatMapConcat { _ =>
        val results = source.poll()
        Source.fromIterator(() => results.toSeq.iterator)
      }
      .map { case (id, content) => new ProducerRecord(topic, id, content) }
      .via(killSwitch.flow)
      .runWith(Producer.plainSink(producerSettings))

    done.onComplete {
      case Success(_) => logger.info(s"Producer ${topic} completed successfully")
      case Failure(e) => logger.error(s"Producer ${topic} completed with error", e)
    }
  }

  def close(): Unit = {
    killSwitch.shutdown()
  }
}

object GenericProducer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val config: Config = ConfigFactory.load()

  val brokers: String = config.getString("kafka.brokers")

}

