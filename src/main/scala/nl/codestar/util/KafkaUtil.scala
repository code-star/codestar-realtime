package nl.codestar.util

import java.util

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}

object KafkaUtil {

  import spray.json._

  implicit val stringDeserializer: Deserializer[String] = new StringDeserializer
  implicit val stringSerializer: Serializer[String] = new StringSerializer

  implicit def jsonDeserializer[T: JsonFormat]: Deserializer[T] = new Deserializer[T] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def close(): Unit = ()

    override def deserialize(topic: String, data: Array[Byte]): T = JsonParser(data).convertTo[T]
  }

  implicit def jsonSerializer[T: JsonFormat]: Serializer[T] = new Serializer[T] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def close(): Unit = ()

    override def serialize(topic: String, data: T): Array[Byte] = data.toJson.compactPrint.getBytes

  }

  def flattenTuple[A, B, C]: (((A, B), C)) => (A, B, C) = {
    case ((a, b), c) => (a, b, c)
  }

  def kafkaConsumerSettings[K: Deserializer, V: Deserializer](implicit actorSystem: ActorSystem): ConsumerSettings[K, V] =
    ConsumerSettings(actorSystem, implicitly[Deserializer[K]], implicitly[Deserializer[V]])

  def kafkaProducerSettings[K: Serializer, V: Serializer](implicit actorSystem: ActorSystem): ProducerSettings[K, V] =
    ProducerSettings(actorSystem, implicitly[Serializer[K]], implicitly[Serializer[V]])
}
