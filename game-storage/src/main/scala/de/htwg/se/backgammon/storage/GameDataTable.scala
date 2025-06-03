package de.htwg.se.backgammon.storage

import slick.jdbc.PostgresProfile.api._
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.base.Field
import slick.lifted.ProvenShape
import de.htwg.se.backgammon.core.base.database.GameData


class GameDataTable(tag: Tag) extends Table[GameData](tag, "game_data") {
  def id        = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name    = column[String]("name")
  def fields    = column[String]("fields")
  def barWhite  = column[Int]("bar_white")
  def barBlack  = column[Int]("bar_black")
  def whoseTurn = column[String]("whose_turn")

  private type RowTuple = (Int, String, String, Int, Int, String)

  // Mapping: Tupel -> GameData
  private val toGameData: RowTuple => GameData = {
    case (id, name, fieldsStr, white, black, turnStr) =>
      val parsedFields = FieldList.from(fieldsStr).fields
      val turn         = Player.withName(turnStr)
      GameData(id, name, parsedFields, white, black, turn)
  }

  // Mapping: GameData -> Tupel
  private val fromGameData: GameData => Option[RowTuple] = { gd =>
    val fieldList = FieldList(gd.fields)
    Some((gd.id, gd.name, fieldList.to(), gd.barWhite, gd.barBlack, gd.whoseTurn.toString()))
  }

  // Slick-Projektion
  override def * = (id, name, fields, barWhite, barBlack, whoseTurn).<>(
    toGameData,
    fromGameData
  )
}