package nl.codestar.data

import spray.json.DefaultJsonProtocol

import scala.xml.{ Node, NodeSeq }

/**
 * Data from NDOV-loket, port 7664, envelope /RIG/NStreinpositiesInterface5
 */

case class TrainLocations(locations: List[TrainLocation]) {

  override def toString: String = s"TrainLocations(\n  $locations\n)"

}

object TrainLocations extends DefaultJsonProtocol {

  // Ad-hoc unmarshalling
  def fromXMl(xml: Node): TrainLocations = TrainLocations(xml.child.map(n => TrainLocation.fromXMl(n.child)).toList)

}

/**
 * A train location is composed of a Train Number and one or more Train Material Parts (see example below).
 */
case class TrainLocation(number: Int = -1, parts: List[MaterialPart]) {

  override def toString: String = s"TrainLocation($number: $parts)"

}

object TrainLocation extends DefaultJsonProtocol {

  // Ad-hoc unmarshalling
  def fromXMl(numberParts: NodeSeq): TrainLocation = {
    val number = numberParts.head.text.toInt
    val parts = numberParts.tail.map(_.head.child)
    TrainLocation(number, parts.map(MaterialPart.fromXMl(_)).toList)
  }

}

/**
 * A Train Material Part is composed of the following elements:
 *   "MaterieelDeelNummer:Materieelvolgnummer:GpsDatumTijd:Orientatie:Bron:Fix:Berichttype:Longitude:Latitude:Elevation:Snelheid:Richting:Hdop:AantalSatelieten"
 * In English: Material Part Number: Material sequence number: GPS DateTime: Orientation: Source: Fix: Message type: Longitude: Latitude: Elevation: Speed: Direction: Hdop: NumberSatelites
 */
case class MaterialPart(
  number: Int = 0,
  sequenceNumber: Int = 0,
  gpsDatetime: String = "",
  orientation: Int = 0,
  source: String = "",
  fix: Int = 0,
  messageType: String = "",
  longitude: Double = 0.0,
  latitude: Double = 0.0,
  elevation: Double = 0.0,
  speed: Double = 0.0,
  direction: Double = 0.0,
  hdop: Double = 0.0,
  numSatellites: Int = 0
)

object MaterialPart extends DefaultJsonProtocol {

  // Ad-hoc unmarshalling
  def fromXMl(ns: NodeSeq): MaterialPart = {
    val p = ns.map(_.text)
    MaterialPart(p.head.toInt, p(1).toInt, p(2).toString, p(3).toInt, p(4).toString, p(5).toInt, p(6).toString,
      p(7).toDouble, p(8).toDouble, p(9).toDouble, p(10).toDouble, p(11).toDouble, p(12).toDouble, p(13).toInt)
  }

}

// XML example:
//"""
//  |<?xml version="1.0" encoding="UTF-8"?>
//  |<tns3:ArrayOfTreinLocation>
//  | <tns3:TreinLocation>
//  |    <tns3:TreinNummer>11654</tns3:TreinNummer>
//  |    <tns3:TreinMaterieelDelen>
//  |      <tns3:MaterieelDeelNummer>4029</tns3:MaterieelDeelNummer>
//  |      <tns3:Materieelvolgnummer>1</tns3:Materieelvolgnummer>
//  |      <tns3:GpsDatumTijd>2018-12-04T15:27:24Z</tns3:GpsDatumTijd>
//  |      <tns3:Orientatie>0</tns3:Orientatie>
//  |      <tns3:Bron>NTT</tns3:Bron>
//  |      <tns3:Fix>1</tns3:Fix>
//  |      <tns3:Berichttype/>
//  |      <tns3:Longitude>5.15445183333</tns3:Longitude>
//  |      <tns3:Latitude>52.2854896667</tns3:Latitude>
//  |      <tns3:Elevation>0.0</tns3:Elevation>
//  |      <tns3:Snelheid>52.0</tns3:Snelheid>
//  |      <tns3:Richting>326.17</tns3:Richting>
//  |      <tns3:Hdop>0</tns3:Hdop>
//  |      <tns3:AantalSatelieten>12</tns3:AantalSatelieten>
//  |    </tns3:TreinMaterieelDelen>
//  |    <tns3:TreinMaterieelDelen>
//  |      <tns3:MaterieelDeelNummer>4052</tns3:MaterieelDeelNummer>
//  |      <tns3:Materieelvolgnummer>2</tns3:Materieelvolgnummer>
//  |      <tns3:GpsDatumTijd>2018-12-04T15:27:23Z</tns3:GpsDatumTijd>
//  |      <tns3:Orientatie>0</tns3:Orientatie>
//  |      <tns3:Bron>NTT</tns3:Bron>
//  |      <tns3:Fix>1</tns3:Fix>
//  |      <tns3:Berichttype/>
//  |      <tns3:Longitude>5.155177</tns3:Longitude>
//  |      <tns3:Latitude>52.284753</tns3:Latitude>
//  |      <tns3:Elevation>0.0</tns3:Elevation>
//  |      <tns3:Snelheid>52.0</tns3:Snelheid>
//  |      <tns3:Richting>330.54</tns3:Richting>
//  |      <tns3:Hdop>0</tns3:Hdop>
//  |      <tns3:AantalSatelieten>12</tns3:AantalSatelieten>
//  |    </tns3:TreinMaterieelDelen>
//  |  </tns3:TreinLocation>
//  |  ...
//  |<tns3:ArrayOfTreinLocation>
//"""