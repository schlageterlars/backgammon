package de.htwg.se.backgammon.core

trait IMove {
  def whereToGo(game: IGame): Int

  def from: Int
  def steps: Int
}
