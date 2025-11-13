package de.htwg.se.backgammon.controller.base

import de.htwg.se.backgammon.util.Observable
import de.htwg.se.backgammon.util.Event
import de.htwg.se.backgammon.util.Manager
import de.htwg.se.backgammon.model.IModel
import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.IMove
import de.htwg.se.backgammon.model.Player
import de.htwg.se.backgammon.model.IDice
import de.htwg.se.backgammon.exception.NotYourFieldException
import de.htwg.se.backgammon.exception.WrongDirectionException
import de.htwg.se.backgammon.exception.DieNotExistException
import de.htwg.se.backgammon.exception.FieldDoesNotExistException
import de.htwg.se.backgammon.model.base.MOVES_PER_ROUND
import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.base.Dice
import de.htwg.se.backgammon.model.base.Model


import scala.util.Try
import scala.util.Failure
import scala.util.Success

import de.htwg.se.backgammon.controller.strategy.ValidateMoveStrategy
import de.htwg.se.backgammon.model.GameState
import de.htwg.se.backgammon.model.base.NoMove
import de.htwg.se.backgammon.exception.NoMoveException
import de.htwg.se.backgammon.controller.IController
import de.htwg.se.backgammon.controller.PutCommand
import de.htwg.se.backgammon.model.base.BearInMove
import de.htwg.se.backgammon.model.base.Move
import scala.util.boundary

case class Controller(private val model: IModel) extends IController {
  def game = model.game
  def previousGame = model.previousGame
  def currentPlayer = model.player
  def dice = model.dice
  def die: Int = dice(0)

  val manager = new Manager[IGame, GameState]
  def doAndPublish(doThis: IMove => Try[IGame], move: IMove): Unit = {
    if checkMove(move) then
      doThis(move).match {
        case Success(game: IGame) => handle(game, move.steps)
        case Failure(exception) =>
          notifyObservers(Event.InvalidMove, Some(exception))
      }
  }

  def init(game: IGame): Unit = {
    this.game = game
  }

  def doAndPublish(doThis: IMove => Try[IGame]): Unit =
    manager.stackCommand match {
      case Some(command: PutCommand) => doAndPublish(doThis, command.move)
      case _ => notifyObservers(Event.InvalidMove, Some(NoMoveException()))
    }

  def undoAndPublish(doThis: => Option[GameState]): Unit = {
    val (game, move) = doThis match {
      case None                        => return
      case Some(GameState(game, move)) => (game, move)
    }
    if (model.dice.length == MOVES_PER_ROUND) {
      model.dice = List(move.steps)
      nextTurn()
    } else {
      model.dice = model.dice.::(move.steps)
    }

    this.game = game
  }

  def handle(game: IGame, steps: Int) = {
    this used steps
    this.game = game
    if (model.dice.isEmpty) {
      if !model.doublets then nextTurn()
      roll()
    }
    if (game.winner.isDefined) then notifyObservers(Event.GameOver)
  }

  def skip(steps: Int): IGame = {
    handle(game, steps); game
  }
  def put(move: IMove): Try[IGame] = manager.doStep(game, PutCommand(move))
  def redo(move: IMove): Try[IGame] = manager.redoStep(game)
  def undo: Option[GameState] = manager.undoStep()
  def quit: Unit = notifyObservers(Event.Quit)

  override def toString = game.toString

  private def game_=(game: IGame) = {
    model.game = game; notifyObservers(Event.Move)
  }

  private def nextTurn() = {
    model.next; notifyObservers(Event.PlayerChanged)
  }

  private def roll(): List[Int] = {
    model.roll
    notifyObservers(Event.DiceRolled)
    if checkersInBar then notifyObservers(Event.BarIsNotEmpty)
    else if hasToBearOff then notifyObservers(Event.AllCheckersInTheHomeBoard)
    model.dice
  }

  def checkersInBar =
    if (currentPlayer == Player.White) game.barWhite > 0 else game.barBlack > 0

  def hasToBearOff =
    game.numberOfPieces(currentPlayer) == game
      .homeBoard(currentPlayer)
      .filter(_.occupier == currentPlayer)
      .map(_.number)
      .sum

  private def used(dice: Int) = model.dice =
    model.dice.patch(model.dice.indexOf(dice), Nil, 1)

  private def checkMove(move: IMove): Boolean =
    ValidateMoveStrategy(this, move).execute() match {
      case Failure(ex: Exception) =>
        notifyObservers(Event.InvalidMove, Some(ex)); false
      case _ => true
    }

  def existsPossibleMoves: Boolean = {
    val movePossible = if (checkersInBar) {
      val move = BearInMove(currentPlayer, die)
      ValidateMoveStrategy(this, move)
        .execute()
        .fold(_ => false, _ => game.move(move).isSuccess)
    } else {
      game.fields.zipWithIndex
        .filter { case (field, _) => field.isOccupiedBy(currentPlayer) }
        .exists { case (field, index) =>
          val move = Move(index, die)
          ValidateMoveStrategy(this, move)
            .execute()
            .fold(_ => false, _ => game.move(move).isSuccess)
        }
    }

    movePossible
  }

  def data = model
}
