package nl.codestar.data

import akka.NotUsed
import akka.stream.scaladsl.Source
import nl.codestar.model.VehicleInfo

/**
  * A source of data from where we can poll.
  */
trait DataSourceGenerator {
  def source: Source[(String, VehicleInfo), NotUsed]
}
