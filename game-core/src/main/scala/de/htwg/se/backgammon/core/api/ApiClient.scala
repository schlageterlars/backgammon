package de.htwg.se.backgammon.core.api

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import com.typesafe.config.ConfigFactory
import play.api.libs.json.Reads
import de.htwg.se.backgammon.core.api.PlayJsonSupport._
import play.api.libs.json.Writes
import akka.http.scaladsl.marshalling.Marshal


class ApiClient(baseUrl: String)(implicit system: ActorSystem) {
  private implicit val ec: ExecutionContext = system.dispatcher
  private val logger = LoggerFactory.getLogger(getClass.getName)
  private val http = Http(system)

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, s"shutdown-client-${baseUrl.hashCode}") { () =>
    shutdown
  }

  def getRequest(endpoint: String): Future[String] =
    sendRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = baseUrl.concat(endpoint)
      )
    )


  def postRequestWithResult[Req: Writes, Res: Reads](endpoint: String, body: Req): Future[Either[Throwable, Res]] = {
    for {
        entity <- Marshal(body).to[RequestEntity]
        request = HttpRequest(HttpMethods.POST, uri = baseUrl + endpoint, entity = entity)
        response <- Http().singleRequest(request)
        result <- {
        if (response.status.isSuccess()) {
            // Try to parse the response body
            Unmarshal(response.entity).to[Res]
            .map(Right(_))
            .recover { case parseErr => Left(new RuntimeException(s"Parsing error: ${parseErr.getMessage}", parseErr)) }
        } else {
            // Read response body (often contains error details) and wrap it in Left
            Unmarshal(response.entity).to[String].map { errorBody =>
            Left(new RuntimeException(s"HTTP ${response.status.intValue()}: $errorBody"))
            }.recover {
            case parseErr => Left(new RuntimeException(s"HTTP ${response.status.intValue()} and failed to read error body: ${parseErr.getMessage}", parseErr))
            }
        }
        }
    } yield result
  }

  def postRequest[Req: Writes](endpoint: String, body: Req): Future[Either[Throwable, Unit]] = {
    for {
        entity <- Marshal(body).to[RequestEntity]
        request = HttpRequest(HttpMethods.POST, uri = baseUrl + endpoint, entity = entity)
        response <- Http().singleRequest(request)
        result <- {
        if (response.status.isSuccess()) {
            Future.successful(Right(()))
        } else {
            Unmarshal(response.entity).to[String].map { errorBody =>
            Left(new RuntimeException(s"HTTP ${response.status.intValue()}: $errorBody"))
            }.recover {
            case ex => Left(new RuntimeException(s"HTTP ${response.status.intValue()} (error body read failed): ${ex.getMessage}", ex))
            }
        }
        }
    } yield result
  }

  private def sendRequest(request: HttpRequest): Future[String] =
    http.singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          Unmarshal(response.entity).to[String]
        case _ =>
          Unmarshal(response.entity).to[String].flatMap { body =>
            val errorMsg = s"HTTP ERROR: ${response.status} - ${request.uri} - $body"
            logger.error(errorMsg)
            Future.failed(new RuntimeException(errorMsg))
          }
      }
    }

  private def shutdown: Future[Done] = {
    logger.info(s"Shutting Down ApiClient for $baseUrl...")
    http.shutdownAllConnectionPools().map(_ => Done)
  }
}

object ApiClient {
  def apply(baseUrl: String): ApiClient = {
    implicit val system: ActorSystem = ActorSystem("ApiClientSystem")
    new ApiClient(baseUrl)
  }
}
