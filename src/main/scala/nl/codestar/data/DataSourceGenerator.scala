package nl.codestar.data

import akka.Done
import akka.stream.scaladsl.Source
import nl.codestar.model.VehicleInfo

import scala.concurrent.Future

/**
  * A source of data from where we can poll.
  */
trait DataSourceGenerator {
  def source: Source[(String, VehicleInfo), Future[Done]]
}
