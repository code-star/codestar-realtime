package nl.codestar.feeds

import akka.Done
import akka.stream.scaladsl.Source
import nl.codestar.model.VehicleInfo

import scala.concurrent.Future

/**
  * A source of streaming [[VehicleInfo]] data
  */
trait VehicleInfoSource {
  def source: Source[(String, VehicleInfo), Future[Done]]
}
