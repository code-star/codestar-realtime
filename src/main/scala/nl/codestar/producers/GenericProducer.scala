package nl.codestar.producers

import java.util.{ Calendar, Properties }

import com.typesafe.config.{ Config, ConfigFactory }
import nl.codestar.data.DataSourceGenerator
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }
import org.slf4j.{ Logger, LoggerFactory }

class GenericProducer(topic: String, source: DataSourceGenerator) {
  import GenericProducer._

  //  val max_request_size: String = (5 * 1024 * 1024).toString

  private val producer = new KafkaProducer[String, Array[Byte]](configuration)

  private def configuration: Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)
    //    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, max_request_size)
    props
  }

  def sendPeriodically(millis: Int = 1000): Unit = {
    while (true) {
      sendOnce()
      Thread.sleep(millis)
    }
  }

  def sendOnce(): Unit = {
    val all = source.poll()
    val records = all.map { case (id, content) => new ProducerRecord(topic, id, content) }
    records.foreach(producer.send)
    producer.flush()
    logger.info(s"${Calendar.getInstance().getTime}: Sent ${records.size} records.")
  }

  def close(): Unit = {
    producer.flush()
    producer.close()
  }

}

object GenericProducer {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val config: Config = ConfigFactory.load()

  val brokers: String = config.getString("kafka.brokers")

}

