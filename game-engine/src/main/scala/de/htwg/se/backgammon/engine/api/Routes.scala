package de.htwg.se.backgammon

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
import de.htwg.se.backgammon.core.IController
import de.htwg.se.backgammon.util.ObserverHttp
import de.htwg.se.backgammon.core.api.PlayJsonSupport._
import de.htwg.se.backgammon.core.base.database.GameData
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.Event

implicit def playJsonMarshaller[T](implicit writes: Writes[T]): ToEntityMarshaller[T] =
Marshaller.withFixedContentType(ContentTypes.`application/json`) { obj =>
    val js = Json.toJson(obj)
    HttpEntity(ContentTypes.`application/json`, Json.stringify(js))
}

class Routes(val controller: IController){
  private val logger = LoggerFactory.getLogger(getClass.getName.init)

  def routes: Route = {
    concat(
      handlePreConnectRequest,
      handleGameDataRequests,
      handlePublishRequests,
      handleInitGameRequest,
      handleRegisterObserverRequest,
      handleDeregisterObserverRequest,
      handleLoadRequest
    )
  }

  private def handlePreConnectRequest: Route = get {
    path("status") {
      complete(StatusCodes.OK)
    }
  }

  private def handleGameDataRequests: Route = get {
    pathPrefix("get") {
      concat(
        path("game") { 
          complete(controller.game)
        },
        path("currentPlayer") { 
          complete(controller.currentPlayer)
        }, 
        path("hasToBearOff") { 
          complete(controller.hasToBearOff)
        }, 
        path("existsPossibleMoves") { 
          complete(controller.existsPossibleMoves)
        }, 
        path("checkersInBar") { 
          complete(controller.checkersInBar)
        }, 
        path("data") { 
          complete(controller.data)
        }, 
        path("dice") { 
          complete(controller.dice)
        }, 
        path("die") { 
          complete(controller.die)
        }, 
        path("gameEnded") { 
          complete(controller.game.winner.isDefined)
        },
      )
    }
  }

  private def handlePublishRequests: Route = post { 
    path("publish") {
      entity(as[JsValue]) { json =>
        println(s"Publish incoming request: ${json}...")
        val method: String = (json \ "method").as[String]
        method match {
          case "put" =>
            val from: Int = (json \ "from").as[Int]
            val steps: Int = (json \ "steps").as[Int]
            controller.doAndPublish(controller.put, Move(from, steps))
            complete(StatusCodes.OK)
          case "skip" =>
            val steps: Int = (json \ "steps").as[Int]
            controller.skip(steps)
            complete(StatusCodes.OK)
          case "undo" =>
            controller.undoAndPublish(controller.undo)
            complete(StatusCodes.OK)
          case "redo" =>
            controller.doAndPublish(controller.redo)
            complete(StatusCodes.OK)
          case "load" =>
            /*controller.load match {
            case Success(_) =>
                complete(StatusCodes.OK)
            case Failure(ex) =>
                complete(StatusCodes.InternalServerError, s"Failed to load model: ${ex.getMessage}")
            }
          case _ =>
            complete(BadRequest, "Invalid method")*/
            complete(StatusCodes.OK)
        }
      }
    }
  }

  private def handleInitGameRequest: Route = post { 
    path("initGame") {
      entity(as[String]) { json =>
        val jsonValue: JsValue = Json.parse(json)

        val numberOfFieldsValidation = (jsonValue \ "numberOfFields").validate[Int] match {
            case JsSuccess(v, _) => v
            case JsError(_) => None
        }

        val numberOfFiguresValidation = (jsonValue \ "numberOfFigures").validate[Int] match {
            case JsSuccess(v, _) => v
            case JsError(_) => None
        }

        (numberOfFieldsValidation, numberOfFiguresValidation) match {
        case (numberOfFields: Int, numberOfFigures: Int) =>
            val setup = DefaultSetup(numberOfFields, numberOfFigures)
            controller.init(Game(Game.create(setup)), Player.White)
            complete(StatusCodes.OK)
        case (None, _) =>   complete(StatusCodes.BadRequest, "Invalid board size.")
        case (_, None) =>   complete(StatusCodes.BadRequest, "Invalid number of pieces.")
        }
      }
    }
  }

  private def handleLoadRequest: Route = post {
    path("init") {
      entity(as[GameData]) { data =>
      controller.init(Game(data.fields, barWhite = data.barWhite, barBlack = data.barBlack), data.whoseTurn)
      controller.notifyObservers(Event.Move)
      println("New game state were set.")
      complete(StatusCodes.OK)
      }
    }
  }

  private def handleRegisterObserverRequest: Route = post { 
    path("registerObserver") {
      entity(as[JsValue]) { json =>
        val observerUrl: String = (json \ "url").as[String]
        controller.add(new ObserverHttp(observerUrl))
        println(s"Observer registered at: $observerUrl")
        logger.info(s"Observer registered at: $observerUrl")
        complete(StatusCodes.OK)
      }
    }
  }

  private def handleDeregisterObserverRequest: Route = post { 
    path("deregisterObserver") {
      entity(as[String]) { json =>
        val jsonValue: JsValue = Json.parse(json)
        val observerUrl: String = (jsonValue \ "url").as[String]
        controller.remove(observerUrl)
        logger.info(s"Observer deregistered from: $observerUrl")
        complete(StatusCodes.OK)
      }
    }
  }
}