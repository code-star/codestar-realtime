package com.ordina.codestar.data

import com.google.transit.realtime.GtfsRealtime

class VehiclesReader(url: String) extends GtfsRealtimeReader(url) {

  def fetchAll(): Iterable[(String, GtfsRealtime.FeedEntity)] = {
    //    for (
    //      entity : FeedEntity <- data if entity.hasVehicle;
    //      id : String <- entity.getIdBytes if !busCompanies.contains(id.split(":")(1))
    //    ) yield id
    data
      .filter(_.hasVehicle)
      .map(entity => (entity.getId, entity))
  }

}
