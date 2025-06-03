package de.htwg.se.backgammon.storage

import slick.jdbc.PostgresProfile.api._
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.base.Field
import slick.lifted.ProvenShape
import de.htwg.se.backgammon.storage.GameDataTable
import slick.lifted.ForeignKeyQuery
import de.htwg.se.backgammon.core.base.database.GameData

final case class GameEntry(id: Int, name: String, gameDataId: Int)

class GameEntryTable(tag: Tag) extends Table[GameEntry](tag, "game_entry") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def gameDataId = column[Int]("game_data_id")

  def gameDataFk: ForeignKeyQuery[GameDataTable, GameData] =
    foreignKey("fk_game_data", gameDataId, TableQuery[GameDataTable])(_.id, onDelete = ForeignKeyAction.Cascade)

  override def * = (id, name, gameDataId) <> (GameEntry.apply, GameEntry.unapply)
}