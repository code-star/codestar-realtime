package nl.codestar.util

import java.util

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}

object KafkaUtil {

  import spray.json._

  implicit val stringDeserializer: Deserializer[String] = new StringDeserializer
  implicit val stringSerializer: Serializer[String]     = new StringSerializer

  /**
    * Implicitly defines a Kafka [[Deserializer]] for any type for which a [[JsonReader]] is in scope
    */
  implicit def jsonDeserializer[T: JsonReader]: Deserializer[T] = new Deserializer[T] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def close(): Unit = ()

    override def deserialize(topic: String, data: Array[Byte]): T = JsonParser(data).convertTo[T]
  }

  /**
    * Implicitly defines a Kafka [[Serializer]] for any type for which a [[JsonFormat]] is in scope
    */
  implicit def jsonSerializer[T: JsonWriter]: Serializer[T] = new Serializer[T] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def close(): Unit = ()

    override def serialize(topic: String, data: T): Array[Byte] = data.toJson.compactPrint.getBytes

  }

  def flattenTuple3[A, B, C]: (((A, B), C)) => (A, B, C) = {
    case ((a, b), c) => (a, b, c)
  }

  /**
    * Convenience function that generates Kafka [[ConsumerSettings]] given implicit [[Deserializer]]s for types [[K]] and [[V]]
    * in scope
    */
  def kafkaConsumerSettings[K: Deserializer, V: Deserializer](implicit actorSystem: ActorSystem): ConsumerSettings[K, V] =
    ConsumerSettings(actorSystem, implicitly[Deserializer[K]], implicitly[Deserializer[V]])

  /**
    * Convenience function that generates Kafka [[ProducerSettings]] given implicit [[Serializer]]s for types [[K]] and [[V]]
    * in scope
    */
  def kafkaProducerSettings[K: Serializer, V: Serializer](implicit actorSystem: ActorSystem): ProducerSettings[K, V] =
    ProducerSettings(actorSystem, implicitly[Serializer[K]], implicitly[Serializer[V]])
}
