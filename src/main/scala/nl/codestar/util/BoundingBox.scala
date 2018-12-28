package nl.codestar.util

import nl.codestar.model.Position

case class BoundingBox(north: Double, east: Double, south: Double, west: Double) {

  def contains(pos: Position): Boolean = {
    val lat = pos.latitude
    val lon = pos.longitude
    south <= lat && lat <= north &&
    west <= lon && lon <= east
  }

}

object BoundingBox {

  val somewhereInUtrecht = BoundingBox(52.10057991947965, 5.166184343397618, 52.07639948922387, 5.094086565077306)

}
