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

case class ProcessRequest(message: String)

class RequestProcessingActor(implicit ec: ExecutionContext) extends Actor {
  def receive: Receive = {
    case ProcessRequest(message) =>
      // Simulate some work done based on the request
      println(s"Processing message: $message")
      sender() ! s"Processed: $message" // Send the result back to the sender
  }
}

object HttpServerWithActor extends App {
  // Define the required implicits
  implicit val system: ActorSystem = ActorSystem("HttpServerWithActor")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  // Create an actor that will process requests
  val requestProcessingActor = system.actorOf(Props(new RequestProcessingActor))

  // Define the HTTP route
  val route = path("process") {
    post {
      entity(as[String]) { message =>
        // Send the incoming request data to the actor
        val result: Future[String] = (requestProcessingActor ? ProcessRequest(message)).mapTo[String]

        // Return the result back to the client
        complete(result)
      }
    }
  }

  // Start the HTTP server
  Http().bindAndHandle(route, "localhost", 8080)
  println("Server is running at http://localhost:8080")
}
