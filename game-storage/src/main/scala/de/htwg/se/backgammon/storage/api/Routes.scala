package de.htwg.se.backgammon.storage.api

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
import de.htwg.se.backgammon.storage.PlayJsonSupport._
import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.storage.JsonStorage

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries._
import de.htwg.se.backgammon.core.base.database.ModelWithNickname
import de.htwg.se.backgammon.core.base.database.GameData
import de.htwg.se.backgammon.storage.dao.GameDataDAO
import scala.concurrent.ExecutionContext
import de.htwg.se.backgammon.storage.api.HttpServer.system


implicit def playJsonMarshaller[T](implicit writes: Writes[T]): ToEntityMarshaller[T] =
Marshaller.withFixedContentType(ContentTypes.`application/json`) { obj =>
    val js = Json.toJson(obj)
    HttpEntity(ContentTypes.`application/json`, Json.stringify(js))
}


class Routes(database: GameDataDAO):
  private val logger = LoggerFactory.getLogger(getClass.getName.init)

  def routes: Route = {
    concat(
      handleGameDataRequests,
      handleSaveRequests,
    )
  }

  private def handleNickname: Route = get {
    path ("setName") {
      parameter("name") { name =>
        println(s"Set nickname to: $name")
        database.setNickname(name)
        complete(StatusCodes.OK)
      }
    }
  }

  private def handleGameDataRequests: Route = get {
    path("load") {
      parameter("name") { name =>
        println(s"Set nickname to: $name")
        database.setNickname(name)
        onComplete(database.findGameDataByNickname(name)) {
          case Success(Some(gameData)) =>
            println(s"Game data found: $gameData")
            complete(gameData)
          case Success(None) =>
            println("No game data found for this nickname.")
            complete(StatusCodes.NotFound)
          case Failure(ex) =>
            println(s"An error occurred: ${ex.getMessage}")
            complete(StatusCodes.InternalServerError)
        }
      }
    }
  }
  private def handleSaveRequests: Route = post {
    path("publish") {
      entity(as[GameData]) { data =>
      print("Try to save data.")
      database.save(data, data.name)
      complete(StatusCodes.OK)
      }
    }
  }


