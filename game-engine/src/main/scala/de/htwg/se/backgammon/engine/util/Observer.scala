package de.htwg.se.backgammon.util

trait Observer:
  def update(e: Event, exception: Option[Throwable]): Unit

trait Observable:
  var subscribers: Vector[Observer] = Vector()
  def add(s: Observer) = subscribers = subscribers :+ s
  def remove(s: Observer) = subscribers = subscribers.filterNot(o => o == s)
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
