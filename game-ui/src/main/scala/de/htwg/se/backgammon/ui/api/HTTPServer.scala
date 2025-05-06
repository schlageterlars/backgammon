package de.htwg.se.backgammon.ui.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import de.htwg.se.backgammon.ui.TUI
import de.htwg.se.backgammon.ui.GUI
import scala.concurrent.Await
import de.htwg.se.backgammon.core.base.Model
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.Dice
import de.htwg.se.backgammon.core.base.DefaultSetup
import de.htwg.se.backgammon.core.Event
import akka.stream.Materializer
import akka.stream.SystemMaterializer

private val NUMBER_OF_FIELDS = 24
private val NUMBER_OF_FIGURES = 15

object ObserverReceiver {
  implicit val system: ActorSystem = ActorSystem("MySystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mat: Materializer = SystemMaterializer(system).materializer

  val controller: HTTPController = HTTPController(Model.default)
  val tui: TUI = new TUI(controller)
  val gui: GUI = new GUI(controller)

   def main(args: Array[String]): Unit = {
    implicit val context = scala.concurrent.ExecutionContext.global
      val f = Future {
        gui.main(Array[String]())
      }
      //tui.run
      Await.ready(f, scala.concurrent.duration.Duration.Inf)
    }

    // Define the route that listens for GET requests with a query parameter
    val route =
      get {
        parameter("event") { string =>
          Event.fromString(string) match {
            case Some(event) => 
              println(s"retrieved event $string")
              controller.onEvent(event)
            case None => complete(StatusCodes.BadRequest, "Invalid event")
          }
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Event '$string' received"))
        }
      }

    // Bind to localhost:9000
    Http().bindAndHandle(route, "localhost", 9000)
    println("Observer receiver is running at http://localhost:9000")
  }