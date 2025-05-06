package de.htwg.se.backgammon.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.actor.ClassicActorSystemProvider
import scala.util.{Success, Failure}
import de.htwg.se.backgammon.engine.api.HttpServerWithActor.system
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import de.htwg.se.backgammon.engine.api.HttpServerWithActor
import akka.http.scaladsl.Http
import de.htwg.se.backgammon.core._


class ObserverHttp(val observerUrl: String) extends HttpObserver {
  override val system: ClassicActorSystemProvider = HttpServerWithActor.system
  override implicit val ec: ExecutionContext = system.classicSystem.dispatcher

  override def update(e: Event, exception: Option[Throwable]): Unit = {
    val requestUrl = s"$observerUrl?event=${e.toString}"
    print(requestUrl)
    val request = HttpRequest(HttpMethods.GET, uri = requestUrl)

    Http()(system.classicSystem).singleRequest(request).onComplete {
      case Success(response) =>
        if (response.status.isSuccess()) {
          println(s"Successfully notified observer: $requestUrl")
        } else {
          println(s"Failed to notify observer: $requestUrl with status: ${response.status}")
        }
      case Failure(ex) =>
        println(s"Error sending HTTP request to $requestUrl: ${ex.getMessage}")
    }
  }
}
