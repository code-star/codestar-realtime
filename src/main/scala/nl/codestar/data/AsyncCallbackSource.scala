package nl.codestar.data

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}

import scala.collection.mutable

object AsyncCallbackSource {
  /**
    * Creates a source that emits an element whenever the provided callback is called
    *
    * This is useful to bridge akka streams to a non-reactive / callback-based API
    */
  def apply[T](cb: AsyncCallback[T] => Unit): Source[T, NotUsed] =
    Source.fromGraph(new AsyncCallbackSourceStage[T](cb))
}

private class AsyncCallbackSourceStage[T](cb: AsyncCallback[T] => Unit) extends GraphStage[SourceShape[T]] {
  val out: Outlet[T] = Outlet("queue")

  override def shape: SourceShape[T] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    var queue: mutable.Queue[T] = new mutable.Queue()

    override def preStart(): Unit = {
      cb(getAsyncCallback[T](queue += _))
    }

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        if (queue.nonEmpty) push(out, queue.dequeue())
      }
    })
  }
}
