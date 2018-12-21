package nl.codestar.data

/**
 *
 * BISON KV6, KV15, KV17:                 tcp://pubsub.besteffort.ndovloket.nl:7658
 * KV78Turbo:                             tcp://pubsub.besteffort.ndovloket.nl:7817
 * NS InfoPlus DVS-PPV:                   tcp://pubsub.besteffort.ndovloket.nl:7664
 * NS AR-NU                               tcp://pubsub.besteffort.ndovloket.nl:7662
 *
 * For more details about the different data streams see: http://data.ndovloket.nl/REALTIME.TXT
 */

/**
 * KV78Turbo documentation: http://data.ndovloket.nl/docs/kv78turbo/Documentatie%20KV78%20turbo-0.5.pdf
 */

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import org.zeromq.{ ZMQ, ZMsg }

import scala.xml._

class OVLoketConnection(port: Int, envelopes: Iterable[String] = Iterable.empty, url: String = "pubsub.besteffort.ndovloket.nl") {

  private val context = ZMQ.context(1)
  private val subscriber = context.socket(ZMQ.SUB)

  subscriber.connect(s"tcp://$url:$port")
  subscribe(envelopes)

  def subscribe(envelopes: Iterable[String]): Unit = envelopes.map(_.getBytes).foreach(subscriber.subscribe)

  def subscribe(envelope: String): Unit = this.subscribe(List(envelope))

  /**
   * @return the message type and the message content as an XML
   */
  def readNext: (String, Node) = {
    val msg = ZMsg.recvMsg(subscriber)
    val msgType = msg.pop.toString
    val rawContent = msg.pop.getData
    val content = unzip(rawContent)
    val xml = scala.xml.XML.loadString(content)
    (msgType, xml)
  }

  def close(): Unit = {
    subscriber.close()
    context.close()
  }

  private def unzip(xs: Array[Byte]): String = {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(xs))
    scala.io.Source.fromInputStream(inputStream).mkString
  }

}

/**
 * BISON (KV6, KV15, KV17)
 * URI: tcp://pubsub.besteffort.ndovloket.nl:7658/
 *
 * Bekende envelopes:
 * /ARR/KV15messages    (Arriva)
 * /ARR/KV17cvlinfo
 * /ARR/KV6posinfo
 * /CXX/KV15messages    (Connexxion, Breng, OV Regio IJsselmond)
 * /CXX/KV17cvlinfo
 * /CXX/KV6posinfo
 * /DITP/KV15messages   (U-OV Sneltram)
 * /DITP/KV17cvlinfo
 * /DITP/KV6posinfo
 * /EBS/KV15messages    (EBS)
 * /EBS/KV17cvlinfo
 * /EBS/KV6posinfo
 * /GVB/KV15messages    (GVB)
 * /GVB/KV17cvlinfo
 * /GVB/KV6posinfo
 * /OPENOV/KV15messages (OpenEBS)
 * /OPENOV/KV17cvlinfo
 * /QBUZZ/KV15messages  (QBuzz)
 * /QBUZZ/KV17cvlinfo
 * /QBUZZ/KV6posinfo
 * /RIG/KV15messages    (HTM, RET, Veolia)
 * /RIG/KV17cvlinfo
 * /RIG/KV6posinfo
 * /SYNTUS/KV6posinfo   (Syntus)
 * /SYNTUS/KV15message
 * /SYNTUS/KV17cvlinfo
 */

/**
 * AR-NU Ritinfo
 * URI: tcp://pubsub.besteffort.ndovloket.nl:7662/
 *
 * Bekende envelopes:
 * /RIG/ARNURitinfo     (NS, Arriva, Syntus, Breng, Veolia)
 */

/**
 * InfoPlus DVS
 * InfoPlus DVS is a message service from the Dutch Railways containing live departure times.
 * URI: tcp://pubsub.besteffort.ndovloket.nl:7664
 *
 * Bekende envelopes:
 * /RIG/InfoPlusDVSInterface4 (DVS PPV)
 * /RIG/InfoPlusPILInterface5
 * /RIG/InfoPlusPISInterface5
 * /RIG/InfoPlusVTBLInterface5
 * /RIG/InfoPlusVTBSInterface5
 * /RIG/NStreinpositiesInterface5
 */

/**
 * This App will print the details of one train location.
 */
//object OVLoketConnection extends App {
//
//  val loket = new OVLoketConnection(port = 7664) // NS InfoPlus DVS-PPV
//  loket.subscribe(List("/RIG/NStreinpositiesInterface5"))
//  //  val envelopesInfoPlus = Seq(
//  //    "/RIG/InfoPlusDVSInterface4",
//  //    "/RIG/InfoPlusPILInterface5",
//  //    "/RIG/InfoPlusPISInterface5",
//  //    "/RIG/InfoPlusVTBLInterface5",
//  //    "/RIG/InfoPlusVTBSInterface5",
//  //    "/RIG/NStreinpositiesInterface5"
//  //  )
//  //  loket.subscribe(envelopesInfoPlus)
//
//  //  while (true) {
//  println(s"Receiving...")
//  val (msgType, xml) = loket.readNext
//
//  val locations = TrainLocations.fromXMl(xml)
//  println(locations)
//
////  import TrainLocationsJsonProtocol._
////  import spray.json._
////  println(locations.toJson)
//
//  loket.close()
//  //    Thread.sleep(1000)
//  //  }
//
//  /**
//   * A train location is composed of a Train Number and one or more Train Material Parts (see example below).
//   * A Train Material Part is composed of the following elements:
//   *   "MaterieelDeelNummer:Materieelvolgnummer:GpsDatumTijd:Orientatie:Bron:Fix:Berichttype:Longitude:Latitude:Elevation:Snelheid:Richting:Hdop:AantalSatelieten"
//   * In English: Material Part Number: Material sequence number: GPS DateTime: Orientation: Source: Fix: Message type: Longitude: Latitude: Elevation: Speed: Direction: Hdop: NumberSatelites
//   *
//   * @param location A train location
//   * @return
//   */
//  def getTrainLocationInfo(location: Node) = {
//    val cs: NodeSeq = location.child
//    val number = cs.head
//    val parts = cs.tail
//    val partsDetails = parts
//      .map(p => (p.label, p.child))
//      .map { case (label, details) => (label, details.map(d => (d.label, d.text))) }
//    (number.text, partsDetails)
//  }
//
//  def getTrainLocation(location: Node) = {
//    val cs: NodeSeq = location.child
//    val number = cs.head
//    val firstPart = cs.tail.head
//    val lat: String = firstPart.child.filter(_.label == "Latitude").head.text
//    val lon: String = firstPart.child.filter(_.label == "Longitude").head.text
//    (number.text, List(lat, lon))
//  }
//
//  def printTrainLocation(train: Node): Unit = {
//    val (number, parts) = getTrainLocation(train)
//    println(s"#$number: ${parts.mkString(":")}")
//  }
//
//}
