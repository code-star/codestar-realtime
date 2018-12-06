package nl.codestar.consumers.positions

import akka.actor.{Actor, ActorLogging, Props}
import nl.codestar.data.Position
import nl.codestar.util.BoundingBox

import spray.json._

object PositionsActor {
  final case class GetByBox(box: BoundingBox)
  final case class GetByDistance(pos: Position, distance: Double)
  final case class GetById(id: String)

  def props: Props = Props[PositionsActor]
}

class PositionsActor(topic: String, groupId: String)
  extends Actor with ActorLogging with VehiclesMapJsonSupport {
  import PositionsActor._

  private val consumer = new PositionsConsumer(topic, groupId)

  private val lastPositionsCache = VehiclesMap.empty

  def receive: Receive = {
    case GetByBox(box) =>
      val newData = consumer.poll()
      lastPositionsCache.update(newData)
      val json = lastPositionsCache.filterByBoundingBox(box).toJson
      println(json)
      sender ! json
  }
}
//#user-registry-actor