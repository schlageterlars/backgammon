package de.htwg.se.backgammon.model

import play.api.libs.json.Reads
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import de.htwg.se.backgammon.model.base.Field
import play.api.libs.json.Writes
import play.api.libs.json.Json
import play.api.libs.json.JsNumber

trait IField {

  def +(that: Int): IField

  def -(that: Int): IField

  def isOccupied(): Boolean

  def isEmpty(): Boolean

  infix def hasSameOccupierAs(that: IField): Boolean

  infix def hasDifferentOccupierThen(that: IField): Boolean

  infix def isOccupiedBy(player: Player): Boolean

  infix def isNotOccupiedBy(player: Player): Boolean

  def copy(number: Int): IField

  def number: Int

  def occupier: Player

  def pieces: Int

}

object IField {
  implicit val iFieldReads: Reads[IField] = (json: JsValue) =>
    json.validate[Int].map(pieces => Field(pieces))

  implicit val iFieldWrites: Writes[IField] = Writes(obj => JsNumber(obj.pieces))
}
