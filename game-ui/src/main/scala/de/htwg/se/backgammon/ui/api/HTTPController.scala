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
import de.htwg.se.backgammon.core.base.database.ModelWithNickname
import de.htwg.se.backgammon.core.base.database.GameData
import de.htwg.se.backgammon.core.api.PlayJsonSupport._
import java.time.Instant


case class HTTPController(
  private var model: IModel,
  baseUrl: String = "http://game-engine:8080",
  storageUrl: String = "http://game-storage:8081"
)( implicit val system: ActorSystem,
  val ec: ExecutionContextExecutor,
  val mat: Materializer) extends IController {
  val client = ApiClient(baseUrl)
  val storageClient = ApiClient(storageUrl)

  var name: Option[String] = Option.empty
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
    def changed(e: Event): Boolean = e match {
      case Event.Move | Event.DiceRolled | Event.PlayerChanged => true
      case _                                                   => false
    }

    if (changed(event)) { 
      if name.isDefined then
        save()
      else 
        println("Game changed, would like to save, but no name is defined.")
    }
    notifyObservers(event)
  }

  def init()= {
    load().onComplete {
    case Success(Success(gameData)) =>
      println(s"Game data loaded: $gameData") 
      val jsonString = Json.stringify(Json.toJson(gameData)) // Convert GameData to JSON string
      val entity = HttpEntity(ContentTypes.`application/json`, jsonString) // Create the HTTP entity
      val request = HttpRequest(HttpMethods.POST, uri = s"$baseUrl/init", entity = entity) // Build the request
      Http().singleRequest(request) // Send it
    case Success(Failure(exception)) =>
      println(s"Game data loading failed: ${exception.getMessage}")
    case Failure(futureException) =>
      println(s"Future failed: ${futureException.getMessage}")
  }
  }

    
  def load(): Future[Try[GameData]] = {
    val name = this.name.getOrElse(return Future.successful(Failure(new RuntimeException("Name not defined"))))
    val uri = s"$storageUrl/load?name=$name"
    val request = HttpRequest(method = HttpMethods.GET, uri = uri)

    Http().singleRequest(request).flatMap { response =>
      if (response.status == StatusCodes.OK) {
        Unmarshal(response.entity).to[GameData].map(Success(_))
      } else if (response.status == StatusCodes.NotFound) {
        Future.successful(Failure(new RuntimeException("No game data found")))
      } else {
        Future.successful(Failure(new RuntimeException(s"Unexpected status: ${response.status}")))
      }
    }.recover {
      case ex => Failure(ex)
    }
  }

  def save(): Unit = {
    val name = this.name.getOrElse(return)
    val game = this.game
    val data = GameData(id = 0, name, game.fields, game.barWhite, game.barBlack, this.data.player, Instant.now.getEpochSecond)
    val json = Json.toJson(data)
    val entity = HttpEntity(ContentTypes.`application/json`, json.toString())

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = s"$storageUrl/publish",
      entity = entity
    )

    Http().singleRequest(request).map { response =>
      if (response.status == StatusCodes.OK) {
        println("game data successfully saved.")
        // parse response to model
        Success(IGame.default)
      } else {
        Failure(new RuntimeException(s"Unexpected status code: ${response.status}"))
      }
    }.recover {
      case ex => Failure(ex)
    }
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
    client.postRequest[JsObject]("/registerObserver", Json.obj("url" -> "http://game-ui:9000")).map {
      case Right(_) =>
        println("Client is succeesfully registered.")
      case Left(error) =>
        println(s"Client is not registered. Request failed with error: ${error.getMessage}")
        error.printStackTrace()
    }
  }

  override def init(game: Game, whoseTurn: Player): Unit = {}  
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