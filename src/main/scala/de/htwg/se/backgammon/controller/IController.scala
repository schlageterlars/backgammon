package de.htwg.se.backgammon.controller

import de.htwg.se.backgammon.util.Observable
import de.htwg.se.backgammon.model.GameState
import de.htwg.se.backgammon.model.Player
import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.IMove
import scala.util.Try
import de.htwg.se.backgammon.model.IModel

trait IController extends Observable {
  def game: IGame
  def previousGame: IGame
  def currentPlayer: Player
  def dice: List[Int]
  def die: Int
  def checkersInBar: Boolean
  def hasToBearOff: Boolean

  def doAndPublish(doThis: IMove => Try[IGame], move: IMove): Unit
  def doAndPublish(doThis: IMove => Try[IGame]): Unit
  def undoAndPublish(doThis: => Option[GameState]): Unit
  def skip(steps: Int): IGame
  def init(game: IGame): Unit
  def put(move: IMove): Try[IGame]
  def redo(move: IMove): Try[IGame]
  def undo: Option[GameState]
  def quit: Unit

  def existsPossibleMoves: Boolean
  def data: IModel
}
