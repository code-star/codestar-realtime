package nl.codestar.util

import nl.codestar.data.Position

/**
  * Distance calculator in the WGS-84 system (World Geodetic System, version 1984)
  */
trait DistanceCalculator {

  def calculateInKilometers(x: Position, y: Position): Int

  def calculateInMeters(x: Position, y: Position): Int

}

object Distance extends DistanceCalculator {

  private val AVERAGE_RADIUS_OF_EARTH_KM = 6371
  private val AVERAGE_RADIUS_OF_EARTH_M = AVERAGE_RADIUS_OF_EARTH_KM * 1000

  private def haversineDistance(l1: Position, l2: Position): Double = {
    val latDistance = Math.toRadians(l1.latitude - l2.latitude)
    val lngDistance = Math.toRadians(l1.longitude - l2.longitude)
    val sinLat = Math.sin(latDistance / 2)
    val sinLng = Math.sin(lngDistance / 2)
    val a = sinLat * sinLat +
      (Math.cos(Math.toRadians(l1.latitude)) *
        Math.cos(Math.toRadians(l2.latitude)) *
        sinLng * sinLng)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    c
  }

  override def calculateInKilometers(l1: Position, l2: Position): Int = {
    (AVERAGE_RADIUS_OF_EARTH_KM * haversineDistance(l1,l2)).toInt
  }

  override def calculateInMeters(l1: Position, l2: Position): Int = {
    (AVERAGE_RADIUS_OF_EARTH_M * haversineDistance(l1,l2)).toInt
  }

}