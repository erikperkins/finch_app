package utils

import io.circe.Json
import io.circe.parser.parse
import scala.io.Source
import scala.util.Properties.envOrElse

package object json {
  val Environment: String = envOrElse("FINCH_ENV", "development")
  val ConfigFile: String = "src/main/config/config.json"
  val Config: Json = read(ConfigFile)

  def read(filename: String): Json = {
    val source = Source.fromFile(filename)
    try parse(source.getLines.mkString).getOrElse(Json.Null) finally source.close()
  }

  def port: String = {
    val bind = Config.hcursor.downField(Environment).get[Int]("port")
      .getOrElse(Json.Null)

    s":$bind"
  }
}
