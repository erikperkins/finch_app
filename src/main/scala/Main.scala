import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.circe.Json
import io.finch.{/, Endpoint, Ok, path}
import io.finch.circe.encodeCirce
import io.finch.syntax.get
import latentdirichlet.{Classifier, Trainer}
import org.mongodb.scala.MongoClient
import utils.config.{mongo, port}
import utils.RequestLogger

object Main extends App {
  val Routes = root :+: topics :+: terms

  val Mongo: MongoClient = MongoClient(mongo)

  val Policy: Cors.Policy = Cors.Policy(
    allowsOrigin = _ => Some("*"),
    allowsMethods = _ => Some(Seq("GET", "POST")),
    allowsHeaders = _ => Some(Seq("Accept"))
  )

  val CORS = new Cors.HttpFilter(Policy)

  val FinchService: Service[Request, Response] =
    CORS andThen RequestLogger andThen Routes.toService

  def root: Endpoint[String] = get(/) {
    Ok("Hello, Finch!")
  }

  def topics: Endpoint[Json] = get("lda") {
    val classifier = new Classifier()
    Ok(classifier.topics)
  }

  def terms: Endpoint[Json] = get("lda" :: path[String]) {
    slug: String => {
      val trainer = new Trainer(Mongo)
      Ok(trainer.message(slug))
    }
  }

  Await.ready(Http.server.serve(port, FinchService))
}
