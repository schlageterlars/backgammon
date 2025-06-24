package de.htwg.se.backgammon.storage.dao

import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.{Future, ExecutionContext}
import de.htwg.se.backgammon.storage.GameDataTable
import de.htwg.se.backgammon.storage.FieldList
import de.htwg.se.backgammon.storage.GameEntry
import de.htwg.se.backgammon.storage.GameEntryTable
import de.htwg.se.backgammon.core.base.database.GameData

class SlickGameDataDAO(db: Database)(implicit ec: ExecutionContext) extends GameDataDAO {

  val gameDataTable = TableQuery[GameDataTable]
  val gameEntryTable = TableQuery[GameEntryTable]

  override def save(data: GameData, nickname: String): Future[Int] = {
    insert(data, nickname)
  }


  override def findGameDataByNickname(nickname: String): Future[Option[GameData]] = {
    val query = for {
      (entry, game) <- gameEntryTable join gameDataTable on (_.gameDataId === _.id)
      if entry.name === nickname
    } yield game

    db.run(query.result.headOption)
  }

  override def updateGameData(id: Int, updatedGameData: GameData): Future[Int] = {
    val updateQuery = gameDataTable.filter(_.id === id)
      .map(g => (g.fields, g.barWhite, g.barBlack, g.whoseTurn))
      .update((FieldList(updatedGameData.fields).to(), updatedGameData.barWhite, updatedGameData.barBlack, updatedGameData.whoseTurn.toString()))

    db.run(updateQuery)
  }

  override def insert(gameData: GameData, nickname: String): Future[Int] = {
    val insertAction = for {
      gameId <- (gameDataTable returning gameDataTable.map(_.id)) += gameData
      _ <- gameEntryTable += GameEntry(0, nickname, gameId)
    } yield gameId

    db.run(insertAction.transactionally)
  }
   
  override def name: String = "slick"

  var nickname: String = ""

  override def getNickname(): String = nickname

  override def setNickname(name: String): Unit =  {
    this.nickname = name
  }
}
