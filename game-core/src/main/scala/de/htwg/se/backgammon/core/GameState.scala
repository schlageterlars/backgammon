package de.htwg.se.backgammon.core

import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.NoMove

case class GameState(game: IGame, move: IMove) {
  def isValid = !move.isInstanceOf[NoMove]
}

object GameState {
  def invalid = GameState(new Game(List()), NoMove())
}
