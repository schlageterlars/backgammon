package de.htwg.se.backgammon.model

enum Player {
  case White
  case Black
  case None

  def toLowerCase = toString.toLowerCase

  def other = if (this == White) Black else White

  def isSameAs(other: Player) = this == None || other == None || this == other

  def isDifferentThan(other: Player) = !(this isSameAs other)
}

object Player {
  def withName(name: String): Player = name match {
    case white if name == "White" => White
    case black if name == "Black" => Black
    case _                        => None
  } 
}
