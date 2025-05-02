package de.htwg.se.backgammon.storage

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, Json}
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import play.api.libs.json._
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.DefaultSetup
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.server.Directives._
import de.htwg.se.backgammon.core.base.Model
import PlayJsonSupport._
import de.htwg.se.backgammon.core.IModel


implicit def playJsonMarshaller[T](implicit writes: Writes[T]): ToEntityMarshaller[T] =
Marshaller.withFixedContentType(ContentTypes.`application/json`) { obj =>
    val js = Json.toJson(obj)
    HttpEntity(ContentTypes.`application/json`, Json.stringify(js))
}

class Routes:
  private val logger = LoggerFactory.getLogger(getClass.getName.init)

  def routes: Route = {
    concat(
      handleGameDataRequests,
      handleSaveRequests
    )
  }

  private def handleGameDataRequests: Route = get {
    path("load") {
      entity(as[String]) { path =>
        val storage = JsonStorage()

        val model = storage.load[IModel](Some("data")) match {
        case Success(obj: Model) => obj
        case _ => None
        } 
        model match {
        case model: Model => complete(model)
        case _ =>            complete(StatusCodes.InternalServerError, s"Failed to load model")

        }
    }
  }
  }

  private def handleSaveRequests: Route = post { 
    path("publish") {
      entity(as[Model]) { data =>
        val model: Model = data
        val storage = JsonStorage()
        storage.save(model, "data")

        complete(StatusCodes.OK)
      }
    }
  }


