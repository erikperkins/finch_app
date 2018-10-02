package latentdirichlet

import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.{SingleObservable, Document}
import org.mongodb.scala.model.Filters._
import io.circe.Json
import io.circe.parser.parse
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

class Trainer(mongo: MongoClient) {
  val Database: MongoDatabase = mongo.getDatabase("newsgroups")
  val Collection: MongoCollection[Document] = Database.getCollection("messages")
  val Wait = Duration(5, TimeUnit.SECONDS)

  def message(term: String): Json = {
    val first: SingleObservable[Document] =
      Collection.find(regex("body", term, "i")).first()

    val document = Await.result(first.toFuture(), Wait) match {
      case result: Document => parse(result.toJson).getOrElse(Json.Null)
      case _ => Json.Null
    }

    document match {
      case Json.Null => absent(term)
      case _ => related(document, term)
    }
  }

  private def related(document: Json, term: String): Json = {
    val body = document.hcursor.get[String]("body").getOrElse("")
    val terms = Random.shuffle(TextProcessor.tokenize(body).toList)
      .take(10).distinct.toArray

    response(term, terms)
  }

  private def absent(term: String): Json = {
    response(term, Array(s"$term is not present in the text corpus"))
  }

  private def response(term: String, values: Array[String]): Json = {
    val slug = Json.fromString(term)
    val datum = Json.fromValues(values.map(Json.fromString))
    Json.obj("slug" -> slug, "datum" -> datum)
  }
}
