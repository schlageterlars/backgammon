package de.htwg.se.backgammon.util

import scala.concurrent.Future

trait Observer:
  def update(e: Event, exception: Option[Throwable]): Unit

class ObserverHttp(observerUrl: String) extends Observer:
  import akka.stream.ActorMaterializer
  import scala.io.Source
  import scala.util.Using
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model._
  import akka.http.scaladsl.model.HttpRequest
  import akka.http.scaladsl.model.HttpMethods._
  import akka.util.Timeout
  import scala.concurrent.ExecutionContext.Implicits.global

  def id: String = observerUrl
  override def update(e: Event, exception: Option[Throwable]): Unit = {
    val baseUrl = id
    val requestUrl = s"$baseUrl?event=${e.toString.toLowerCase}"

    // Create the HTTP GET request
    val request = HttpRequest(GET, uri = requestUrl)

    // Send the request asynchronously using Akka HTTP
    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

    // Handle the response asynchronously
    responseFuture.onComplete {
      case Success(response) =>
        if (response.status.isSuccess()) {
          println(s"Successfully notified observer: $requestUrl")
        } else {
          println(s"Failed to notify observer: $requestUrl with status: ${response.status}")
        }
      case Failure(exception) =>
        println(s"Error sending HTTP request to $requestUrl: ${exception.getMessage}")
    }
  }  

object ObserverHttp {
  def unapply(observer: ObserverHttp): Option[String] = Some(observer.id)
}


trait Observable:
  var subscribers: Vector[Observer] = Vector()
  def add(s: Observer) = subscribers = subscribers :+ s
  def remove(s: Observer) = subscribers = subscribers.filterNot(o => o == s)
  def remove(observerUrl: String): Unit = {
  subscribers = subscribers.filter {
    case ObserverHttp(url) => url != observerUrl
    case _ => true
   }
 }
  def notifyObservers(e: Event, exception: Option[Throwable] = None) =
    subscribers.foreach(o => o.update(e, exception))

enum Event:
  case Quit
  case Move
  case BarIsNotEmpty
  case InvalidMove
  case PlayerChanged
  case DiceRolled
  case AllCheckersInTheHomeBoard
  case GameOver
