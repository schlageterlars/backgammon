package de.htwg.se.backgammon.model

import de.htwg.se.backgammon.model.storage.Storable
import scala.util.Try
import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes
import de.htwg.se.backgammon.model.base.Game
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import scala.xml.{Elem, Null, Text, TopScope}


trait IGame extends IndexedSeq[IField] with Storable {
  def fields: List[IField]
  def barWhite: Int
  def barBlack: Int

  def move(move: IMove): Try[IGame]

  def get(position: Int): IField

  def winner: Option[Player]

  def homeBoard: Map[Player, List[IField]]

  def numberOfPieces: Map[Player, Int]

  def bar: Map[Player, Int]

  def ==(that: IGame): Boolean

  def !=(that: IGame): Boolean

  override def asXml: Elem = {
  val barWhiteElem: Elem = Elem(
    prefix = null,
    label = "barWhite",
    attributes = Null,
    scope = TopScope,
    minimizeEmpty = true,
    child = Text(barWhite.toString)
  )

  val barBlackElem: Elem = Elem(
    prefix = null,
    label = "barBlack",
    attributes = Null,
    scope = TopScope,
    minimizeEmpty = true,
    child = Text(barBlack.toString)
  )

  val fieldElems: Seq[Elem] = fields.map { field =>
    Elem(
      prefix = null,
      label = "field",
      attributes = Null,
      scope = TopScope,
      minimizeEmpty = true,
      child = Text(field.pieces.toString)
    )
  }

  val fieldsElem: Elem = Elem(
    prefix = null,
    label = "fields",
    attributes = Null,
    scope = TopScope,
    minimizeEmpty = true,
    child = fieldElems*
  )

  Elem(
    prefix = null,
    label = "game",
    attributes = Null,
    scope = TopScope,
    minimizeEmpty = true,
    child = barWhiteElem, barBlackElem, fieldsElem
  )
}
}

object IGame {
  implicit val gameReads: Reads[IGame] = Game.gameReads.map(identity[IGame])
  implicit val gameWrites: Writes[IGame] = (game: IGame) =>
    Game.gameWrites.writes(game.asInstanceOf[Game])
}
