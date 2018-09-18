package utils

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.slf4j.{Logger, LoggerFactory}
import org.slf4j.impl.SimpleLogger
import utils.config.logLevel

abstract class RequestLogger[REQ <: Request]
  extends SimpleFilter[REQ, Response] {

  val DateTimeFormat: String = "[yyyy-MM-dd'T'HH:mm:ss.SSSZ]"

  val logger: Logger = {
    System.setProperty(SimpleLogger.SHOW_DATE_TIME_KEY, "true")
    System.setProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, DateTimeFormat)
    System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel)
    LoggerFactory.getLogger("Request")
  }

  def apply(request: REQ, service: Service[REQ, Response]): Future[Response] = {
    logger.info(s"${request.method} ${request.uri} ${request.params}")
    service(request)
  }
}

object RequestLogger extends RequestLogger[Request]
