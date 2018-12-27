package nl.codestar.feeds

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import org.zeromq.{ZMQ, ZMsg}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}
import akka.actor.typed.scaladsl.adapter._

object ZeroMqSource {

  /**
    * Creates a [[Source]] with messages from ZeroMQ
    *
    * @param url URL of the ZeroMQ server
    * @param port Port of the ZeroMQ server
    * @param envelopes Envelopes to subscribe to
    * @param pollInterval Interval to check for new messages
    * @param bufferSize Amount of messages the source can buffer before failing
    * @return Source that materializes to a function that will close the ZeroMQ connection
    */
  def apply(url: String, port: Int, envelopes: Seq[String], pollInterval: FiniteDuration = 50.millis, bufferSize: Int = 100)(implicit ec: ExecutionContext,
                                                                                                                             actorSystem: ActorSystem): Source[ZMsg, () => Unit] =
    Source.actorRef(bufferSize, OverflowStrategy.fail).mapMaterializedValue { sourceActor =>
      val actor = actorSystem.spawnAnonymous(behavior(url, port, envelopes, pollInterval, sourceActor.toTyped[ZMsg]))
      () =>
        actor ! Stop
    }

  /**
    * Actor that polls ZeroMQ for available messages and forwards them to another actor for further processing
    *
    * The ZMQ server is polled periodically according to the `pollInterval`. When more messages are available
    * immediately, they are read immediately.
    *
    * @param url
    * @param port
    * @param envelopes
    * @param pollInterval
    * @param receiver
    * @return
    */
  private def behavior(url: String, port: Int, envelopes: Seq[String], pollInterval: FiniteDuration, receiver: ActorRef[ZMsg], nrIoThreads: Int = 1): Behavior[Command] =
    Behaviors.setup { ctx =>
      Behaviors.withTimers { timers =>
        val zmqContext = ZMQ.context(nrIoThreads)
        val socket     = zmqContext.socket(ZMQ.SUB)
        val address    = s"tcp://$url:$port"

        socket.connect(address)
        envelopes.map(_.getBytes).foreach(socket.subscribe)

        @tailrec
        def receiveAllAvailable(): Unit = {
          val msg = Option(ZMsg.recvMsg(socket, ZMQ.DONTWAIT | ZMQ.NOBLOCK))
          msg.foreach(receiver ! _)
          if (msg.isDefined) receiveAllAvailable() else ()
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

  private sealed trait Command
  private case object DoReceive extends Command
  private case object Stop      extends Command
}
