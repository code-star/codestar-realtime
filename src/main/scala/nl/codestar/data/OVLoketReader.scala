package nl.codestar.data

import java.io.{ ByteArrayOutputStream, ObjectOutputStream }

//import spray.json._

class OVLoketReader(port: Int, envelopes: Iterable[String]) extends DataSource with TrainLocationsJsonSupport {

  private val conn = new OVLoketConnection(port = port, envelopes = envelopes)

  def poll(): Map[String, Array[Byte]] = {
    val (_, xml) = conn.readNext

    val locations = TrainLocations.fromXMl(xml).locations
    locations
      .map(l => (l.number.toString, l.parts))
      .toMap
      .mapValues(_.map(p => (p.latitude, p.longitude)))
      .mapValues(serialise)

  }

  def serialise(value: Any): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.toByteArray
  }

  //  def deserialise(bytes: Array[Byte]): Any = {
  //    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
  //    val value = ois.readObject
  //    ois.close()
  //    value
  //  }

}
