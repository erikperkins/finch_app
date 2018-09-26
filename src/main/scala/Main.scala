import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.Json
import io.finch.{/, Endpoint, Ok, path}
import io.finch.circe.encodeCirce
import io.finch.syntax.get
import latentdirichlet.Classifier
import org.mongodb.scala.MongoClient
import utils.config.port
import utils.RequestLogger

object Main extends App {
  val routes = home :+: topics :+: terms
  val Mongo: MongoClient = MongoClient("mongodb://storage.datapun.net:27017")

  def home: Endpoint[String] = get(/) {
    Ok("Hello, Finch!")
  }

  def topics: Endpoint[Json] = get("lda") {
    val classifier = new Classifier()
    Ok(classifier.topics)
  }

  def terms: Endpoint[Json] = get("lda" :: path[String]) {
    slug: String => {
      val classifier = new Classifier()
      Ok(classifier.terms(slug))
    }
  }

  Await.ready(Http.server.serve(port, RequestLogger andThen routes.toService))
}
