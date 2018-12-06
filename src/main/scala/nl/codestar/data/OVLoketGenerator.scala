package nl.codestar.data

//import java.io.{ ByteArrayOutputStream, ObjectOutputStream }

import spray.json._

class OVLoketGenerator(port: Int, envelopes: Iterable[String])
  extends DataSourceGenerator
//    with TrainLocationsJsonSupport
    with VehicleInfoJsonSupport {

  private val conn = new OVLoketConnection(port, envelopes)

  def poll(): Map[String, Array[Byte]] = {
    val (_, xml) = conn.readNext

    TrainLocations.fromXMl(xml).locations
      .map(l => (l.number.toString, l.parts))
      .toMap
      .mapValues(_.map(p => VehicleInfo(p.latitude, p.longitude, VehicleInfo.gpsTimeToMillis(p.gpsDatetime))))
      .mapValues(_.toJson.toString)
      .mapValues(_.toArray.map(_.toByte))
//      .mapValues(serialise)
  }

//  def serialise(value: Any): Array[Byte] = {
//    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
//    val oos = new ObjectOutputStream(stream)
//    oos.writeObject(value)
//    oos.close()
//    stream.toByteArray
//  }

//  def deserialise(bytes: Array[Byte]): Any = {
//    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
//    val value = ois.readObject
//    ois.close()
//    value
//  }

}
