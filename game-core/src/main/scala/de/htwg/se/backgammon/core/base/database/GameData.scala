package de.htwg.se.backgammon.core.base.database

import play.api.libs.json.Reads
import play.api.libs.json.Writes
import de.htwg.se.backgammon.core.Player
import play.api.libs.json.Json
import de.htwg.se.backgammon.core.IField

final case class GameData(id: Int, name: String, fields: List[IField], barWhite: Int = 0, barBlack: Int = 0, whoseTurn: Player)

object GameData {
  implicit val modelReads: Reads[GameData] = Json.reads[GameData]
  implicit val modelWrites: Writes[GameData] = Json.writes[GameData]
}
