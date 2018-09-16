package latentdirichlet

import io.circe.Json
import io.circe.parser.parse
import utils.json.read

class Classifier {
  def topics: Json = {
    read("src/main/resources/topics.json")
  }

  def terms(term: String): Json = {
    term match {
      case "delorean" => read("src/main/resources/delorean.json")
      case _ => notFound(term)
    }
  }

  private def notFound(term: String): Json = {
    val message: String =
      s"""
        |{
        |  \"slug\":\"$term\",
        |  \"datum\":[\"'$term' is not present in the text corpus\"]
        |}
      """.stripMargin
    parse(message).right.get
  }
}
