package de.htwg.se.backgammon.ui.api

import de.htwg.se.backgammon.core.GameState
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.IMove
import de.htwg.se.backgammon.core.IController
import scala.util.Try
import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.Event

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.{Future, ExecutionContextExecutor}
import scala.util.{Try, Success, Failure}
import play.api.libs.json._
import scala.concurrent.Await
import scalafx.util.Duration
import de.htwg.se.backgammon.core.api.ApiClient
import scala.concurrent.duration.DurationInt
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.Observer


case class HTTPController(
  private var model: IModel,
  baseUrl: String = "http://localhost:8080"
)( implicit val system: ActorSystem,
  val ec: ExecutionContextExecutor,
  val mat: Materializer) extends IController {

  val client = ApiClient(baseUrl)

  override def game: IGame = fetchData[IGame]("/get/game")

  override def currentPlayer: Player = fetchData[Player]("/get/currentPlayer")

  override def hasToBearOff: Boolean = fetchData[Boolean]("/get/hasToBearOff")

  override def existsPossibleMoves: Boolean = fetchData[Boolean]("/get/existsPossibleMoves")

  override def checkersInBar: Boolean = fetchData[Boolean]("/get/checkersInBar")

  override def data: IModel = fetchData[IModel]("/get/data")

  override def dice: List[Int] = fetchData[List[Int]]("/get/dice")
  
  override def die: Int = fetchData[Int]("/get/die")

  override def previousGame: IGame = {
    IGame.default
  }

  def onEvent(event: Event) = {
    notifyObservers(event)
  }

  override def put(move: IMove): Try[IGame] = {
    val json = Json.obj(
      "method" -> "put",
      "from" -> move.from,
      "steps" -> move.steps
    )
    val entity = HttpEntity(ContentTypes.`application/json`, json.toString())
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"$baseUrl/publish", entity = entity))
    return Success(IGame.default)
  }

  override def undo: Option[GameState] = {
    Option(GameState(IGame.default, Move(0, 0)))
  }

  override def skip(steps: Int): IGame = {
      val json = Json.obj(
      "method" -> "skip",
      "steps" -> steps
      )
      val entity = HttpEntity(ContentTypes.`application/json`, json.toString())
      Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"$baseUrl/publish", entity = entity))
      return IGame.default
  }

  override def redo(move: IMove): Try[IGame] = {
      return Success(IGame.default)
  }

  override def add(s: Observer): Unit = {
    print("Register observer")
    super.add(s)
    client.postRequest[JsObject]("/registerObserver",  Json.obj("url" -> "http://localhost:9000")) 
  }

  override def init(game: Game): Unit = {}
  
  override def quit: Unit = {}

  override def undoAndPublish(doThis: => Option[GameState]): Unit = {}

  override def doAndPublish(doThis: IMove => Try[IGame]): Unit = {
    print("doAndPublish(doThis: IMove => Try[IGame]) method is not supported yet.")
  }

  override def doAndPublish(doThis: IMove => Try[IGame], move: IMove): Unit = {
    val json = Json.obj(
      "method" -> "put",
      "from" -> move.from,
      "steps" -> move.steps
    )
    val entity = HttpEntity(ContentTypes.`application/json`, json.toString())
    Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = s"$baseUrl/publish", entity = entity))
  }


  def fetchData[T](endpoint: String)(implicit reads: Reads[T]): T = {
    val result = client.getRequest[T](endpoint)
    Await.result(result, 2.seconds) match {
      case Right(data) => data
      case Left(error) =>
        println(s"Failed to fetch data from $endpoint: ${error.getMessage}")
        null.asInstanceOf[T] // Or throw, or return Option[T]
    }
  }
}