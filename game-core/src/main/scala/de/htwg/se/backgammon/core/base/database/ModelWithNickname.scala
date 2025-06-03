package de.htwg.se.backgammon.core.base.database

import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.Json
import de.htwg.se.backgammon.core.base.database.GameData

case class ModelWithNickname(name: String, gameData: GameData)

object ModelWithNickname {
  implicit val modelReads: Reads[ModelWithNickname] = Json.reads[ModelWithNickname]
  implicit val modelWrites: Writes[ModelWithNickname] = Json.writes[ModelWithNickname]
}