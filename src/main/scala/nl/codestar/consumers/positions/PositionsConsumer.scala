package nl.codestar.consumers.positions

import java.util.Properties

import com.google.transit.realtime.GtfsRealtime
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import com.google.transit.realtime.GtfsRealtime.FeedEntity
import spray.json.JsonParser
import nl.codestar.data.{VehicleInfo, VehicleInfoJsonSupport}
import nl.codestar.producers.GenericProducer

import scala.collection.JavaConverters._

class PositionsConsumer(topic: String, groupId: String) extends VehicleInfoJsonSupport {

  private val fetch_max_bytes: String = (5 * 1024 * 1024).toString

  val consumer = new KafkaConsumer[String, Array[Byte]](configuration)
  consumer.subscribe(List(topic).asJava)

  private def configuration: Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, GenericProducer.brokers)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer].getCanonicalName)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetch_max_bytes) // largest size of a message that can be fetched
//    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
//    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    props
  }

  /**
    * Data from OVLoket
    */
  def poll(): Map[String, VehicleInfo] = this.synchronized {
    val records = consumer.poll(3 * 1000)
    records.asScala
      .groupBy(_.key)
      .mapValues(_.map(_.value.map(_.toChar).mkString))       // Convert each sequence of Array[Byte] values to a sequence of Strings.
      .mapValues(_.map(JsonParser(_).convertTo[VehicleInfo])) // Parse each String in the sequence into Json and unmarshal it to VehicleInfo.
      .mapValues(_.toSeq.sortWith(_.time > _.time))           // Sort the VehicleInfo values by time.
      .mapValues(_.head)                                      // Take the newest VehicleInfo, that is, the one with greater time.
  }

  /**
    * Data from OpenOV
    */
  def pollGTFS(): Map[String, GtfsRealtime.Position] = this.synchronized {
    val records = consumer.poll(3 * 1000)

    val entities: Map[String, FeedEntity] = records.asScala
      .groupBy(_.key)
      .mapValues(_.last.value)
      .mapValues(FeedEntity.parseFrom)

    val map = entities
      .filter { case (_, e) => e.hasVehicle }
      .mapValues(_.getVehicle)
      .filter { case (_, vehicle) => vehicle.hasPosition }
      .mapValues(_.getPosition)

    map
  }

}