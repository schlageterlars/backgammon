package de.htwg.se.backgammon.model.strategy

import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.Player

abstract class BearInMoveStrategy(game: IGame, player: Player, to: Int)
    extends MoveCheckersStrategy(game) {
  override def pickUpCheckers: IGame = bar(player).--
}

abstract class DefaultMoveStrategy(game: IGame, from: Int, to: Int)
    extends MoveCheckersStrategy(game) {
  override def pickUpCheckers: IGame = set(from -> (game(from) - 1))
}

class BearOffMoveStrategy(game: IGame, from: Int, to: Int)
    extends IMoveStrategy(game) {
  def execute(): IGame = set(from -> (game(from) - 1))
}
