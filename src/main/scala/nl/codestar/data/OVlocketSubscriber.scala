package nl.codestar.data

import org.zeromq.ZMQ

object OVlocketSubscriber extends App {

  val context = ZMQ.context(1)
  val subscriber = context.socket(ZMQ.SUB)

  subscriber.connect("tcp://pubsub.besteffort.ndovloket.nl:7658") // BISON KV6, KV15, KV17
  subscriber.subscribe("/ARR/KV15messages".getBytes())
  subscriber.subscribe("/ARR/KV17cvlinfo".getBytes())
  subscriber.subscribe("/ARR/KV6posinfo".getBytes())
  subscriber.subscribe("/SYNTUS/KV6posinfo".getBytes())

  //  subscriber.connect("tcp://pubsub.besteffort.ndovloket.nl:7817/") // KV78Turbo
  //  subscriber.subscribe("/RIG/ARNURitinfo".getBytes())

  //  subscriber.connect("tcp://pubsub.besteffort.ndovloket.nl:7664/") // NS InfoPlus DVS-PPV

  //  subscriber.connect("tcp://pubsub.besteffort.ndovloket.nl:7662/") // NS AR-NU

  while (true) {
    val data = new String(subscriber.recvStr())
    println(s"Message received: $data")
    Thread.sleep(1000)
  }

}
