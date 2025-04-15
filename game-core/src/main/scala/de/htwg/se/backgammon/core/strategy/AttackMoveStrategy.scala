package de.htwg.se.backgammon.model.strategy

import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.Player
import de.htwg.se.backgammon.model.base.Field

trait AttackMoveStrategy(game: IGame, attacker: Player, to: Int)
    extends MoveCheckersStrategy {
  override def placeCheckers: IGame = {
    set(to -> Field(attacker))
    bar(defender).++
  }

  def defender = attacker.other
}