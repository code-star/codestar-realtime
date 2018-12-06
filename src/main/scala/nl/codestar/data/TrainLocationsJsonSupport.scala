package nl.codestar.data

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{ DefaultJsonProtocol, JsonFormat }

/**
 * Provide JsonFormats for our case classes in TrainLocations; all other JsonFormats (fdr Int, List, etc) are implicit.
 * lazyFormat was needed because of a NullPointerException.
 * See: https://github.com/spray/spray-json
 */
trait TrainLocationsJsonSupport extends SprayJsonSupport {

  // import the default encoders for primitive types (Int, String, Lists, etc)
  import DefaultJsonProtocol._

  implicit val trainLocationsFormat: JsonFormat[TrainLocations] = lazyFormat(jsonFormat1(TrainLocations.apply))
  implicit val trainLocationFormat: JsonFormat[TrainLocation] = lazyFormat(jsonFormat2(TrainLocation.apply))
  implicit val materialPartFormat: JsonFormat[MaterialPart] = lazyFormat(jsonFormat14(MaterialPart.apply))

}