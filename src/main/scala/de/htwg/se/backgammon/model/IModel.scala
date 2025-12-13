package de.htwg.se.backgammon.model

import scala.xml.Elem

trait IModel {

  def next: Player

  def doublets: Boolean

  var dice: List[Int]

  def roll: List[Int]

  def game_=(game: IGame): Unit

  def game: IGame

  var movesThisRound: List[IGame]

  def previousGame: IGame

  def player: Player
}
