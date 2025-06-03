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
import scala.concurrent.duration.DurationInt
import akka.http.scaladsl.server.Route
import scala.util.{Success, Failure}
import de.htwg.se.backgammon.ui.component.BackgammonNicknameDialog
import scalafx.application.Platform
import de.htwg.se.backgammon.core.api.ApiClient


private val NUMBER_OF_FIELDS = 24
private val NUMBER_OF_FIGURES = 15

object ObserverReceiver {
  implicit val system: ActorSystem = ActorSystem("MySystem")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mat: Materializer = SystemMaterializer(system).materializer


  //val tui: TUI = new TUI(controller)

   def main(args: Array[String]): Unit = {
    implicit val context = scala.concurrent.ExecutionContext.global

    val targetUrl = "http://game-engine:8080/status"
    val maxRetries = 10
    val retryDelay = 1  .seconds
    
    def waitForServer(attempt: Int = 1): Future[Unit] = {
      println(s"Attempt $attempt: Checking $targetUrl...")

      Http().singleRequest(HttpRequest(uri = targetUrl)).flatMap { response =>
        if (response.status.isSuccess()) {
          response.discardEntityBytes()
          println("✅ Target server is up.")
          Future.successful(())
        } else {
          response.discardEntityBytes()
          retryOrFail(attempt, s"Status code ${response.status}")
        }
      }.recoverWith {
        case ex =>
          retryOrFail(attempt, ex.getMessage)
      }
    }

    def retryOrFail(attempt: Int, reason: String): Future[Unit] = {
      println(s"❌ Attempt $attempt failed: $reason")
      if (attempt >= maxRetries) {
        Future.failed(new RuntimeException("Gave up waiting for server."))
      } else {
        akka.pattern.after(retryDelay, system.scheduler)(waitForServer(attempt + 1))
      }
    }

    // Start the logic
    waitForServer().onComplete {
      case Success(_) =>
        println("🚀 Starting observer server...")
        val controller: HTTPController = HTTPController(Model.default)

        val route: Route =
          get {
            parameter("event") { string =>
              Event.fromString(string) match {
                case Some(event) =>
                  println(s"retrieved event '$string'")
                  controller.onEvent(event)
                  complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"Event '$string' received"))
                case None =>
                  complete(StatusCodes.BadRequest, "Invalid event")
              }
            }
          }

        Http().bindAndHandle(route, "0.0.0.0", 9000).onComplete {
          case Success(_) =>
            println("✅ GUI Service is running at http://game-ui:9000")
            val gui: GUI = new GUI(controller)
            gui.onStart = () => {
                BackgammonNicknameDialog.getNickname() match {
                  case Some(name) => {
                    println(s"Nickname ist: $name")
                    controller.name = Some(name)
                    controller.init()
                  }
                  case None => println("Kein Name eingegeben.")
                }
            }

            val f = Future {
              gui.main(Array[String]())
            } 
            //tui.run
            println("Start ScalaFX GUI. ")
            Await.ready(f, scala.concurrent.duration.Duration.Inf)
          case Failure(ex) =>
            println(s"❌ Failed to start observer server: ${ex.getMessage}")
            system.terminate()
        }

      case Failure(ex) =>
        println(s"❌ Could not start because upstream server never became available: ${ex.getMessage}")
        system.terminate()
    }
  }
}
  