package de.htwg.se.backgammon.core.strategy

import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.IField
import de.htwg.se.backgammon.core.base.Field
import de.htwg.se.backgammon.core.Player

trait PeacefulMoveStrategy(game: IGame, occupier: Player, to: Int)
    extends MoveCheckersStrategy {
  override def placeCheckers: IGame =
    set(
      to -> (if (game(to).isEmpty()) Field(occupier) else (game(to) + 1))
    )
}
