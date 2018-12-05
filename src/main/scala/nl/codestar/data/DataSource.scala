package nl.codestar.data

trait DataSource {

  /**
   * Polling from a feed returns a list of ids and a content as an array of bytes.
   */
  def poll(): Map[String, Array[Byte]]

}