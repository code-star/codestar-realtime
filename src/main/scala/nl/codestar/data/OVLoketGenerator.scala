package nl.codestar.data

import spray.json._

class OVLoketGenerator(port: Int, envelopes: Iterable[String])
  extends DataSourceGenerator
    with VehicleInfoJsonSupport {

  private val conn = new OVLoketConnection(port, envelopes)

  def poll(): Map[String, Array[Byte]] = {
    val (_, xml) = conn.readNext

    def toJsonBytes(v: VehicleInfo): Array[Byte] = v.toJson.toString.toArray.map(_.toByte)

    TrainLocations.fromXMl(xml).locations
      .map(l => (l.number.toString, l.parts))
      .toMap
      .mapValues(_.head)  // Some TrainLocation have more than one MaterialPart; take only the first one.
      .mapValues(p => VehicleInfo(p.latitude, p.longitude, VehicleInfo.gpsTimeToMillis(p.gpsDatetime)))
      .mapValues(toJsonBytes)
  }

}
