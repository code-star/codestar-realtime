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

import scala.xml.{ Elem, Node }
import scala.xml.factory.XMLLoader

class OVLoketConnection(url: String = "pubsub.besteffort.ndovloket.nl", port: Int, envelopes: Iterable[String] = Iterable.empty) {

  private val context = ZMQ.context(1)
  private val subscriber = context.socket(ZMQ.SUB)

  subscriber.connect(s"tcp://$url:$port")
  subscribe(envelopes)

  def subscribe(envelopes: Iterable[String]): Unit = {
    envelopes.map(_.getBytes).foreach(subscriber.subscribe)
  }

  def subscribe(envelope: String): Unit = {
    this.subscribe(List(envelope))
  }

  def readNext: (String, Node) = {
    val msg = ZMsg.recvMsg(subscriber)
    val msgType = msg.pop.toString
    val rawContent = msg.pop.getData
    val content = unzip(rawContent)
    val xml = getXmlNodeFrom(content)
    (msgType, xml)
  }

  private def unzip(xs: Array[Byte]): String = {
    val inputStream = new GZIPInputStream(new ByteArrayInputStream(xs))
    scala.io.Source.fromInputStream(inputStream).mkString
  }

  private def getXmlNodeFrom(str: String): Node = {
    //    val schemaLang = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
    //    val xsdFile = java.nio.file.Paths.get("sitemap-v0.9.xsd")
    //    val readOnly = java.nio.file.StandardOpenOption.READ
    //    val inputStream = java.nio.file.Files.newInputStream(xsdFile, readOnly)
    //    val xsdStream = new javax.xml.transform.stream.StreamSource(inputStream)
    //    val schema = javax.xml.validation.SchemaFactory.newInstance(schemaLang).newSchema(xsdStream)

    val factory = javax.xml.parsers.SAXParserFactory.newInstance()
    factory.setNamespaceAware(true)
    //    factory.setSchema(schema)
    val validatingParser = factory.newSAXParser()
    val loader: XMLLoader[Elem] = new scala.xml.factory.XMLLoader[scala.xml.Elem] {
      override def parser = validatingParser
      override def adapter =
        new scala.xml.parsing.NoBindingFactoryAdapter with scala.xml.parsing.ConsoleErrorHandler

    }
    loader.loadString(str)
  }

  def close(): Unit = {
    subscriber.close()
    context.close()
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
 * URI: tcp://pubsub.besteffort.ndovloket.nl:7664/
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
object OVLoketConnection extends App {

  val loket = new OVLoketConnection(port = 7664) // NS InfoPlus DVS-PPV
  loket.subscribe(List("/RIG/NStreinpositiesInterface5"))
  //  val envelopesInfoPlus = Seq(
  //    "/RIG/InfoPlusDVSInterface4",
  //    "/RIG/InfoPlusPILInterface5",
  //    "/RIG/InfoPlusPISInterface5",
  //    "/RIG/InfoPlusVTBLInterface5",
  //    "/RIG/InfoPlusVTBSInterface5",
  //    "/RIG/NStreinpositiesInterface5"
  //  )
  //  loket.subscribe(envelopesInfoPlus)

  //  while (true) {
  println(s"Receiving...")
  val (msgType, xml) = loket.readNext
  val trainLocations: Seq[Node] = xml.child
  println(s"Message type: $msgType, content size: ${trainLocations.length}")
  trainLocations.foreach(printTrainLocation)
  loket.close()
  //    Thread.sleep(1000)
  //  }

  /**
   * A train location is composed of a Train Number and one or more Train Material Parts (see example below).
   * A Train Material Part is composed of the following elements:
   *   "MaterieelDeelNummer:Materieelvolgnummer:GpsDatumTijd:Orientatie:Bron:Fix:Berichttype:Longitude:Latitude:Elevation:Snelheid:Richting:Hdop:AantalSatelieten"
   * In English: Material Part Number: Material sequence number: GPS DateTime: Orientation: Source: Fix: Message type: Longitude: Latitude: Elevation: Speed: Direction: Hdop: NumberSatelites
   *
   * @param location A train location
   * @return
   */
  def getTrainLocationInfo(location: Node) = {
    val cs: Seq[Node] = location.child
    val number = cs.head
    val parts = cs.tail
    val partsDetails = parts
      .map(p => (p.label, p.child))
      .map { case (label, details) => (label, details.map(d => (d.label, d.text))) }
    (number.text, partsDetails)
  }

  def getTrainLocation(location: Node) = {
    val cs: Seq[Node] = location.child
    val number = cs.head
    val firstPart = cs.tail.head
    val lat: String = firstPart.child.filter(_.label == "Latitude").head.text
    val lon: String = firstPart.child.filter(_.label == "Longitude").head.text
    (number.text, List(lat, lon))
  }

  def printTrainLocation(train: Node): Unit = {
    val (number, parts) = getTrainLocation(train)
    println(s"#$number: ${parts.mkString(":")}")
  }

  private val trainLocationExample =
    """
      |  <tns3:TreinLocation>
      |    <tns3:TreinNummer>11654</tns3:TreinNummer>
      |    <tns3:TreinMaterieelDelen>
      |      <tns3:MaterieelDeelNummer>4029</tns3:MaterieelDeelNummer>
      |      <tns3:Materieelvolgnummer>1</tns3:Materieelvolgnummer>
      |      <tns3:GpsDatumTijd>2018-12-04T15:27:24Z</tns3:GpsDatumTijd>
      |      <tns3:Orientatie>0</tns3:Orientatie>
      |      <tns3:Bron>NTT</tns3:Bron>
      |      <tns3:Fix>1</tns3:Fix>
      |      <tns3:Berichttype/>
      |      <tns3:Longitude>5.15445183333</tns3:Longitude>
      |      <tns3:Latitude>52.2854896667</tns3:Latitude>
      |      <tns3:Elevation>0.0</tns3:Elevation>
      |      <tns3:Snelheid>52.0</tns3:Snelheid>
      |      <tns3:Richting>326.17</tns3:Richting>
      |      <tns3:Hdop>0</tns3:Hdop>
      |      <tns3:AantalSatelieten>12</tns3:AantalSatelieten>
      |    </tns3:TreinMaterieelDelen>
      |    <tns3:TreinMaterieelDelen>
      |      <tns3:MaterieelDeelNummer>4052</tns3:MaterieelDeelNummer>
      |      <tns3:Materieelvolgnummer>2</tns3:Materieelvolgnummer>
      |      <tns3:GpsDatumTijd>2018-12-04T15:27:23Z</tns3:GpsDatumTijd>
      |      <tns3:Orientatie>0</tns3:Orientatie>
      |      <tns3:Bron>NTT</tns3:Bron>
      |      <tns3:Fix>1</tns3:Fix>
      |      <tns3:Berichttype/>
      |      <tns3:Longitude>5.155177</tns3:Longitude>
      |      <tns3:Latitude>52.284753</tns3:Latitude>
      |      <tns3:Elevation>0.0</tns3:Elevation>
      |      <tns3:Snelheid>52.0</tns3:Snelheid>
      |      <tns3:Richting>330.54</tns3:Richting>
      |      <tns3:Hdop>0</tns3:Hdop>
      |      <tns3:AantalSatelieten>12</tns3:AantalSatelieten>
      |    </tns3:TreinMaterieelDelen>
      |  </tns3:TreinLocation>
    """.stripMargin
}
