import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.Json
import io.finch.{Endpoint, Ok, path}
import io.finch.circe.encodeCirce
import io.finch.syntax.get
import utils.json.port
import latentdirichlet.Classifier

object Main extends App {
  val routes = topics :+: terms

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

  Await.ready(Http.server.serve(port, routes.toService))
}
