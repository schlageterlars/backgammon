package de.htwg.se.backgammon.engine.api

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import de.htwg.se.backgammon.Routes
import de.htwg.se.backgammon.engine.controller.base.Controller
import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.core.base.Model

case class ProcessRequest(message: String)

class RequestProcessingActor(implicit ec: ExecutionContext) extends Actor {
  def receive: Receive = {
    case ProcessRequest(message) =>
      // Simulate some work done based on the request
      println(s"Processing message: $message")
      sender() ! s"Processed: $message" // Send the result back to the sender
  }
}
object HttpServerWithActor {
  // Move these out of main
  implicit val system: ActorSystem = ActorSystem("HttpServerWithActor")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  def main(args: Array[String]): Unit = {
    val requestProcessingActor = system.actorOf(Props(new RequestProcessingActor()(executionContext)))

    val controller = Controller(Model.default)
    val routes = new Routes(controller)
    val route = concat( path("process") {
      post {
        entity(as[String]) { message =>
          val result: Future[String] = (requestProcessingActor ? ProcessRequest(message)).mapTo[String]
          complete(result)
        }
      }
    }, routes.routes)

    val kafka = KafkaObserver(controller)
    val bindingFuture =  Http().bindAndHandle(route, "0.0.0.0", 8080)

    bindingFuture.failed.foreach { ex =>
      println(s"Failed to bind HTTP endpoint, terminating system. Reason: ${ex.getMessage}")
      system.terminate()
    }

    println("Server started at http://0.0.0.0:8080")
  }
}