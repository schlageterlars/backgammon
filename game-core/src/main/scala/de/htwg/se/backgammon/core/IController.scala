package de.htwg.se.backgammon.core

import de.htwg.se.backgammon.core.GameState
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.IMove
import scala.util.Try
import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.core.base.Game

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
  def put(move: IMove): Try[IGame]
  def redo(move: IMove): Try[IGame]
  def undo: Option[GameState]
  def quit: Unit
  def init(game: Game): Unit

  def existsPossibleMoves: Boolean
  def data: IModel
}
