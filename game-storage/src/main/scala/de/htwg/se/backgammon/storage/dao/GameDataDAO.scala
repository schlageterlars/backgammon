package de.htwg.se.backgammon.storage.dao

import scala.concurrent.Future
import de.htwg.se.backgammon.core.base.database.GameData

trait GameDataDAO {
  def findGameDataByNickname(nickname: String): Future[Option[GameData]]
  def updateGameData(id: Int, updatedGameData: GameData): Future[Int]
  def insert(gameData: GameData, nickname: String): Future[Int]
  def setNickname(name: String): Unit
  def getNickname(): String

  def save(gameData: GameData, nickname: String): Future[Int]


  def name: String
}
