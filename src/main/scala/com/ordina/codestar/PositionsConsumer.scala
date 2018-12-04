package com.ordina.codestar

import java.util.Properties

import com.google.transit.realtime.GtfsRealtime.{ FeedEntity, Position }

import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecords, KafkaConsumer }
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer }

class PositionsConsumer(brokers: String, topic: String, groupId: String) {

  private val fetch_max_bytes: String = (5 * 1024 * 1024).toString

  val consumer = new KafkaConsumer[String, Array[Byte]](configuration)
  consumer.subscribe(List(topic).asJava)

  private def configuration: Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer].getCanonicalName)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetch_max_bytes) // largest size of a message that can be fetched
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    props
  }

  def fetch(): Map[String, Position] = this.synchronized {
    val records: ConsumerRecords[String, Array[Byte]] =
      consumer.poll(30 * 1000)

    val entities = records
      .asScala
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
