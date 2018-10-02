package utils

import io.circe.Json
import scala.util.Properties.envOrElse
import utils.json.read

package object config {
  val Environment: String = envOrElse("FINCH_ENV", "development")
  val ConfigFile: String = "src/main/config/config.json"
  val Config: Json = read(ConfigFile)

  def port: String = {
    val bind = Config.hcursor.downField(Environment).get[Int]("port")
      .getOrElse(8000)

    s":$bind"
  }

  def logLevel: String = {
    Config.hcursor.downField(Environment).get[String]("logLevel")
      .getOrElse("info")
  }

  def mongo: String = {
    Config.hcursor.downField(Environment).get[String]("mongo")
      .getOrElse("mongodb://localhost:27017")
  }
}
