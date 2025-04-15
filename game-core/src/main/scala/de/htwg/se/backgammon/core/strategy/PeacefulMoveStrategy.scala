package de.htwg.se.backgammon.model.strategy

import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.IField
import de.htwg.se.backgammon.model.base.Field
import de.htwg.se.backgammon.model.Player

trait PeacefulMoveStrategy(game: IGame, occupier: Player, to: Int)
    extends MoveCheckersStrategy {
  override def placeCheckers: IGame =
    set(
      to -> (if (game(to).isEmpty()) Field(occupier) else (game(to) + 1))
    )
}
