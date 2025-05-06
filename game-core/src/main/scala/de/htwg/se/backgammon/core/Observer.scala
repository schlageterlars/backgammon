package de.htwg.se.backgammon.core

import akka.actor.ClassicActorSystemProvider
import scala.concurrent.ExecutionContext

trait Observer:
  def update(e: Event, exception: Option[Throwable]): Unit

trait HttpObserver extends Observer {
  def observerUrl: String
  def system: ClassicActorSystemProvider
  implicit def ec: ExecutionContext

  def update(e: Event, exception: Option[Throwable]): Unit
}

object HttpObserver {
  def unapply(observer: HttpObserver): Option[String] = Some(observer.observerUrl)
}

trait Observable:
  var subscribers: Vector[Observer] = Vector()
  def add(s: Observer) = subscribers = subscribers :+ s
  def remove(s: Observer) = subscribers = subscribers.filterNot(o => o == s)
  def remove(observerUrl: String): Unit = {
  subscribers = subscribers.filter {
    case HttpObserver(url) => url != observerUrl
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

  override def toString: String = productPrefix.head.toLower + productPrefix.tail


object Event:
  def fromString(s: String): Option[Event] =
    values.find(_.toString.equalsIgnoreCase(s))