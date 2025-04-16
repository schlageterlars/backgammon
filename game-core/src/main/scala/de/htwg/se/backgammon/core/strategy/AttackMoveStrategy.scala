package de.htwg.se.backgammon.core.strategy

import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.base.Field

trait AttackMoveStrategy(game: IGame, attacker: Player, to: Int)
    extends MoveCheckersStrategy {
  override def placeCheckers: IGame = {
    set(to -> Field(attacker))
    bar(defender).++
  }

  def defender = attacker.other
}