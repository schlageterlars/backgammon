package de.htwg.se.backgammon.metric.api
import play.api.libs.json.{Json, Writes, Reads}

object GameStats {
    implicit val gameStatsWrites: Writes[GameStats] = Json.writes[GameStats]
    implicit val gameStatsReads: Reads[GameStats] = Json.reads[GameStats]
}

object PlayerStats {
        implicit val playerStatsWrites: Writes[PlayerStats] = Json.writes[PlayerStats]
        implicit val playerStatsReads: Reads[PlayerStats] = Json.reads[PlayerStats]
}

case class PlayerStats(
  avgMoveDuration: Double,
  minMoveDuration: Double,
  maxMoveDuration: Double,
  longestMoveStreak: Int,
  numOfTotalMoves: Int
)

case class GameStats(
  totalDuration: Double,
  playerStats: Map[String, PlayerStats]
)
