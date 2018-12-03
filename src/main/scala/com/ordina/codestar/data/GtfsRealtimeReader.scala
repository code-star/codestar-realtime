package com.ordina.codestar.data

import java.net.URL

import scala.collection.JavaConverters._
import com.google.transit.realtime.GtfsRealtime
import com.google.transit.realtime.GtfsRealtime.FeedMessage

import scala.collection.mutable

class GtfsRealtimeReader (feederUrl: String) {

  private val url = new URL(feederUrl)
  protected val feed: FeedMessage = FeedMessage.parseFrom(url.openStream)
  protected val data: mutable.Buffer[GtfsRealtime.FeedEntity] = feed.getEntityList.asScala

  //TODO: use feed validator: https://github.com/google/transitfeed/wiki/FeedValidator

}
