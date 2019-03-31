package latentdirichlet

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.parse
import java.sql.{Connection, DriverManager, ResultSet, Statement, SQLException}
import scala.collection.mutable.ArrayBuffer
import utils.config.database
import utils.json.read

class Search {
  case class Response(slug: String, datum: Array[String])

  val driver = "org.postgresql.Driver"
  val host = sys.env("POSTGRES_HOST")
  val url = s"jdbc:postgresql://$host:5432/$database"
  val username = sys.env("CLOUDBERRY_USERNAME")
  val password = sys.env("CLOUDBERRY_PASSWORD")

  def terms(term: String): Json = {
    try {
      Class.forName(driver)

      val connection: Connection =
        DriverManager.getConnection(url, username, password)

      val statement: Statement = connection.createStatement
      val cursor: ResultSet = statement.executeQuery(relatedTerms(term))

      val results = new ArrayBuffer[String]
      while (cursor.next) {
        val token = cursor.getString("token")
        results.append(token)
      }

      connection.close()

      Response(term, results.toArray).asJson
    } catch {
      case e: SQLException => {
        e.printStackTrace
        notFound(term)
      }
    }
  }

  def topics: Json = {
    read("src/main/resources/topics.json")
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

  private def relatedTerms(term: String): String =
    s"""
      |select
      |  w.token
      |from term_term_matrix m
      |inner join vocabulary v
      |  on m.i = v.term
      |inner join vocabulary w
      |  on m.j = w.term
      |where v.token = '$term'
      |  and w.token != '$term'
      |order by value desc
      |limit 15
    """.stripMargin
}
