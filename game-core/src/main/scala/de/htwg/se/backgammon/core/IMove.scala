package de.htwg.se.backgammon.model

trait IMove {
  def whereToGo(game: IGame): Int

  def from: Int
  def steps: Int
}
