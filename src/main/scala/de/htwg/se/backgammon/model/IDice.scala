package de.htwg.se.backgammon.model
import de.htwg.se.backgammon.model.base.Dice

trait IDice {
  def roll: Int
  def roll(times: Int): List[Int]
}
