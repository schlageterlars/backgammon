package de.htwg.se.backgammon.core

import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.JsString

enum Player {
  case White
  case Black
  case None

  def toLowerCase = toString.toLowerCase

  def other = if (this == White) Black else White

  infix def isSameAs(other: Player) = this == None || other == None || this == other

  def isDifferentThan(other: Player) = !(this isSameAs other)
}

object Player {

  implicit val playerReads: Reads[Player] = (json: JsValue) =>
    json.validate[String].map(value => Player.withName(value))

  implicit val playerWrites: Writes[Player] = Writes(obj => JsString(obj.toString()))

  def withName(name: String): Player = name match {
    case white if name == "White" => White
    case black if name == "Black" => Black
    case _                        => None
  } 
}
