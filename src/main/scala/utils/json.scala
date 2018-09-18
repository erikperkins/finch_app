package utils

import io.circe.Json
import io.circe.parser.parse
import scala.io.Source

package object json {
  def read(filename: String): Json = {
    val source = Source.fromFile(filename)

    try
      parse(source.getLines.mkString).getOrElse(Json.Null)
    finally
      source.close()
  }
}
