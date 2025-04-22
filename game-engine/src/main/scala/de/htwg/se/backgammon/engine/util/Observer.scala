package de.htwg.se.backgammon.util

trait Observer:
  def update(e: Event, exception: Option[Throwable]): Unit

class ObserverHttp(observerUrl: String) extends Observer:
  import scala.io.Source
  import scala.util.Using

  def id: String = observerUrl
  override def update(e: Event, exception: Option[Throwable]): Unit = {
    val baseUrl = id
    val requestUrl = s"$baseUrl?event=${e.toString.toLowerCase}"
    Using.resource(Source.fromURL(requestUrl)) { source => Some(source.getLines.mkString) }
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
