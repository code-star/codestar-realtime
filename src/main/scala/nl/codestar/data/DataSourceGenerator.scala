package nl.codestar.data

/**
 * A source of data from where we can poll.
 */
trait DataSourceGenerator {

  /**
   * Polling from a feed returns a list of ids and a content as an array of bytes.
   */
  def poll(): Map[String, Array[Byte]]

}