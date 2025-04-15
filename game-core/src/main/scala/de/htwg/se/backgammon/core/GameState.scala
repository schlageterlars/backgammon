package de.htwg.se.backgammon.model

import de.htwg.se.backgammon.model.base.Game
import de.htwg.se.backgammon.model.base.NoMove

case class GameState(game: IGame, move: IMove) {
  def isValid = !move.isInstanceOf[NoMove]
}

object GameState {
  def invalid = GameState(new Game(List()), NoMove())
}
