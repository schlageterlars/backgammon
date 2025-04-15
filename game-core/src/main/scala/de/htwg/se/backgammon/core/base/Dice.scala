package de.htwg.se.backgammon.model.base

import scala.util.Random
import de.htwg.se.backgammon.model.IDice

class Dice extends IDice {
  def roll: Int = Random.nextInt(6) + 1

  def roll(times: Int): List[Int] = List.tabulate(times)(_ => roll)
}
