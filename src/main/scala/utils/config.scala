package utils

import io.circe.Json
import scala.util.Properties.envOrElse
import utils.json.read

package object config {
  val Environment: String = envOrElse("CLOUDBERRY_ENV", "development")
  val ConfigFile: String = "src/main/config/config.json"
  val Config: Json = read(ConfigFile)

  def port: String = {
    val bind = Config.hcursor.downField(Environment).get[Int]("port")
      .getOrElse(8000)

    s":$bind"
  }

  def host: String = {
    Config.hcursor.downField(Environment).get[String]("host")
      .getOrElse("localhost")
  }

  def database: String = {
    Config.hcursor.downField(Environment).get[String]("database")
      .getOrElse("cloudberry")
  }

  def logLevel: String = {
    Config.hcursor.downField(Environment).get[String]("logLevel")
      .getOrElse("info")
  }

  def model: String = {
    Config.hcursor.downField(Environment).get[String]("model")
      .getOrElse("s3a://datapunnet/cloudberry/model")
  }

  def vocab: String = {
    Config.hcursor.downField(Environment).get[String]("vocabulary")
      .getOrElse("s3a://datapunnet/cloudberry/vocabulary")
  }
}
