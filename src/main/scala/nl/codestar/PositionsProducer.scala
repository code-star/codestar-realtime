package nl.codestar

import java.util.{ Calendar, Properties }

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }
import nl.codestar.data.VehiclesReader
import org.slf4j.{ Logger, LoggerFactory }

class PositionsProducer(brokers: String, topic: String, feedUrl: String) {

  private val producer = new KafkaProducer[String, Array[Byte]](configuration)
  private val feederVehicles = new VehiclesReader(feedUrl)

  private def configuration: Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getCanonicalName)
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, PositionsProducer.max_request_size)
    props
  }

  def sendPeriodically(millis: Int = 1000): Unit = {
    while (true) {
      sendOnce()
      Thread.sleep(millis)
    }
  }

  def sendOnce(): Unit = {
    val all = feederVehicles.fetchAll()
    val records = all.map { case (id, vehicle) => new ProducerRecord[String, Array[Byte]](topic, id, vehicle.toByteArray) }

    records.foreach(producer.send)
    producer.flush()

    //    all.filter{ case (id,_) => id.startsWith("2018-12-03:QBUZZ:u008:1080") }
    //    all.filter{ case (id,v) => v.getVehicle.getVehicle.getLabel == "4122" }
    //      .map { case (id, vehicle) => (id, vehicle.getVehicle.getPosition )}
    //      .foreach(println)

    println(s"data size sent: ${records.size}") // at ${Calendar.getInstance().toString}")
  }

  def close(): Unit = {
    producer.flush()
    producer.close()
  }

}

object PositionsProducer extends App {

  private val max_request_size: String = (5 * 1024 * 1024).toString

  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  val config = ConfigFactory.load()

  val brokers = config.getString("brokers")
  val topic = config.getString("feeds.vehiclePositions.topic")
  val vehiclesFeedUrl = config.getString("feeds.vehiclePositions.url")

  val vehiclesProducer = new PositionsProducer(brokers, topic, vehiclesFeedUrl)
  vehiclesProducer.sendPeriodically(60 * 1000)
  vehiclesProducer.close()

}