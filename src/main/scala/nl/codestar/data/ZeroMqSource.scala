package nl.codestar.data

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import nl.codestar.data.ZeroMqSourceActor.Stop
import org.zeromq.{ZMQ, ZMsg}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}

object ZeroMqSourceActor {

  /**
    * Actor that polls ZeroMQ for available messages and forwards them to another actor for further processing
    *
    * @param url
    * @param port
    * @param envelopes
    * @param pollInterval
    * @param receiver
    * @return
    */
  def behavior(url: String, port: Int, envelopes: Seq[String], pollInterval: FiniteDuration, receiver: ActorRef[ZMsg]): Behavior[Command] = Behaviors.setup { ctx =>
    Behaviors.withTimers { timers =>
      val zmqContext = ZMQ.context(1)
      val socket     = zmqContext.socket(ZMQ.SUB)
      val address    = s"tcp://$url:$port"

      socket.connect(address)
      println(s"Connected to ${address} and envelopes ${envelopes.mkString(",")}")
      envelopes.map(_.getBytes).foreach(socket.subscribe)

      @tailrec
      def receiveAllAvailable(): Unit = {
        val msg = Option(ZMsg.recvMsg(socket)) // , ZMQ.DONTWAIT | ZMQ.NOBLOCK))
        msg.foreach(receiver ! _)
        if (msg.isDefined) {
          receiveAllAvailable()
        } else ()
      }

      ctx.self ! DoReceive

      Behaviors.receiveMessage {
        case DoReceive =>
          receiveAllAvailable()

          // Try again later
          timers.startSingleTimer((), DoReceive, pollInterval)
          Behaviors.same
        case Stop =>
          socket.close()
          zmqContext.close()
          Behavior.stopped
      }
    }
  }

  sealed trait Command
  case object DoReceive extends Command
  case object Stop      extends Command
}

import akka.actor.typed.scaladsl.adapter._

object ZeroMqSource {
  def apply(url: String, port: Int, envelopes: Seq[String], pollInterval: FiniteDuration = 50.millis, bufferSize: Int = 100)(implicit ec: ExecutionContext,
                                                                                                                             actorSystem: ActorSystem): Source[ZMsg, () => Unit] =
    Source.actorRef(bufferSize, OverflowStrategy.fail).mapMaterializedValue { sourceActor =>
      val actor = actorSystem.spawnAnonymous(ZeroMqSourceActor.behavior(url, port, envelopes, pollInterval, sourceActor.toTyped[ZMsg]))
      () =>
        actor ! Stop
    }
}
