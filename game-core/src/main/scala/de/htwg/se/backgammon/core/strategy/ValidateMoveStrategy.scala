package de.htwg.se.backgammon.core.strategy

import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.exception.FieldDoesNotExistException
import de.htwg.se.backgammon.exception.AttackNotPossibleException
import de.htwg.se.backgammon.exception.EmptyFieldException
import de.htwg.se.backgammon.core.validate.ValidateStrategy

class ValidateBearInMoveStrategy(
    val game: IGame,
    val player: Player,
    val to: Int
) extends ValidateStrategy {
  def from = if (player == Player.White) -1 else game.length

  override def validate() = {
    require(
      (to < game.length && to >= 0),
      FieldDoesNotExistException(from, (from.abs - to.abs).abs, to)
    )
    require(
      (player == game(to).occupier || game(to).occupier == Player.None || game(
        to
      ).number <= 1),
      AttackNotPossibleException(from, to, game(to).number)
    )
  }
}

class ValidateBearOffMoveStrategy(
    val game: IGame,
    val from: Int,
    val to: Int
) extends ValidateStrategy {

  override def validate() = {
    require(game(from).isOccupied(), EmptyFieldException(from))
    require(
      (to <= game.length && to >= -1),
      FieldDoesNotExistException(from, (from.abs - to.abs).abs, to)
    )
    if (to != game.length && to != -1) {
      require(
        (game(from) hasSameOccupierAs game(to)) || game(to).number <= 1,
        AttackNotPossibleException(from, to, game(to).number)
      )
    }
  }
}

class DefaultValidateMoveStrategy(
    val game: IGame,
    val from: Int,
    val to: Int
) extends ValidateStrategy {

  override def validate() = {
    require(
      (to < game.length && to >= 0),
      FieldDoesNotExistException(from, (from.abs - to.abs).abs, to)
    )

    require(game(from).isOccupied(), EmptyFieldException(from))

    require(
      (game(from) hasSameOccupierAs game(to)) || game(to).number <= 1,
      AttackNotPossibleException(from, to, game(to).number)
    )
  }
}
