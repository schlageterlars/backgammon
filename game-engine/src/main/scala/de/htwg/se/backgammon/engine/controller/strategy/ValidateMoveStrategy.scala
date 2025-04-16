package de.htwg.se.backgammon.controller.strategy

import de.htwg.se.backgammon.core.IMove
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.IField
import de.htwg.se.backgammon.exception.FieldDoesNotExistException
import de.htwg.se.backgammon.exception.MoveException
import de.htwg.se.backgammon.exception.NotYourFieldException
import de.htwg.se.backgammon.exception.BarIsNotEmptyException
import de.htwg.se.backgammon.exception.DieNotExistException
import de.htwg.se.backgammon.core.validate.ValidateStrategy
import scala.util.Try
import de.htwg.se.backgammon.core.base.Field
import de.htwg.se.backgammon.core.base.NoMove
import de.htwg.se.backgammon.core.base.BearInMove
import de.htwg.se.backgammon.exception.NoMoveException
import de.htwg.se.backgammon.controller.IController

trait ValidateMoveStrategy(
    val game: IGame,
    val move: IMove,
    val dice: List[Int]
) extends ValidateStrategy {
  override def validate() = {
    require(dice.contains(move.steps), DieNotExistException(move.steps, dice))
    require(!move.isInstanceOf[NoMove], NoMoveException())
  }
}

class ValidateMove(game: IGame, move: IMove, player: Player, dice: List[Int])
    extends ValidateMoveStrategy(game, move, dice) {
  val field = Try(game.get(move.from)).getOrElse(Field(Player.None))

  override def validate() = {
    super.validate()
    require(game.bar(player) == 0, BarIsNotEmptyException())
    require(
      (field.occupier == player),
      NotYourFieldException(move.from, player, field.occupier)
    )
    require(
      (move.from < game.fields.length && move.from >= 0),
      FieldDoesNotExistException(move.from, move.steps, move.from)
    )
  }
}

class ValidateBearInMove(game: IGame, move: IMove, dice: List[Int])
    extends ValidateMoveStrategy(game, move, dice)

object ValidateMoveStrategy {
  def apply(c: IController, move: IMove): ValidateMoveStrategy =
    move match {
      case _: BearInMove =>
        ValidateBearInMove(c.game, move, c.dice)
      case _ => ValidateMove(c.game, move, c.currentPlayer, c.dice)
    }
}
