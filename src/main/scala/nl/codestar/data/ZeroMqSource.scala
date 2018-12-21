package nl.codestar.data

import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import org.zeromq.ZMQ.Poller
import org.zeromq.{ZLoop, ZMQ, ZMsg}

import scala.concurrent.ExecutionContext

object ZeroMqSource {
  def apply(url: String, port: Int, envelopes: Seq[String])(implicit ec: ExecutionContext): Source[ZMsg, NotUsed] =
    Source.single(ZMQ.context(1)).flatMapConcat { context =>
      val socket  = context.socket(ZMQ.SUB)
      val loop    = new ZLoop(context)
      val address = s"tcp://$url:$port"

      socket.connect(address)
      envelopes.map(_.getBytes).foreach(socket.subscribe)

      AsyncCallbackSource[ZMsg] { callback =>
        loop.addPoller(
          new ZMQ.PollItem(socket, Poller.POLLIN),
          (_, _, _) => {
            val msg = ZMsg.recvMsg(socket)
            callback.invoke(msg)
            0
          },
          null
        )
        loop.start()
      }.watchTermination() {
        case (_, done) =>
          done.map { _ =>
            socket.close()
            context.close()
            Done
          }
      }
    }
}
