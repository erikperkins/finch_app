import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.circe.Json
import io.finch.{/, Endpoint, Ok, path}
import io.finch.circe.encodeCirce
import io.finch.syntax.get
import latentdirichlet.Search
import utils.config.port
import utils.RequestLogger

object Main extends App {
  val Routes = root :+: topics :+: terms

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
    val search = new Search()
    Ok(search.topics)
  }

  def terms: Endpoint[Json] = get("lda" :: path[String]) {
    slug: String => {
      val search = new Search()
      Ok(search.terms(slug))
    }
  }

  Await.ready(Http.server.serve(port, FinchService))
}
