package latentdirichlet

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.parse
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.ml.linalg.{DenseMatrix, Vectors}
import utils.json.read


class Classifier(spark: SparkSession, vocabulary: DataFrame, termTermMatrix: DenseMatrix) {
  import spark.implicits._

  case class Response(slug: String, datum: Array[String])

  def classify(term: String): Json = {
    try {
      val termIndex =
        vocabulary.select("index").filter($"term" === term).first().getLong(0)

      val termVector =
        Vectors.sparse(vocabulary.count.toInt, Array(termIndex.toInt), Array(1))

      val relatedIndices =
        termTermMatrix.multiply(termVector.toDense)
          .toArray
          .zipWithIndex
          .sortBy(_._1)
          .reverse
          .map(_._2)
          .take(20)

      val datum =
        vocabulary.select("term")
          .filter($"index".isin(relatedIndices: _*) && !($"term" === term))
          .rdd.collect.flatMap(_.toSeq.map(_.toString))

      Response(term, datum).asJson
    } catch {
      case e: Exception => notFound(term)
    }
  }

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
    parse(message).getOrElse(Json.Null)
  }
}
