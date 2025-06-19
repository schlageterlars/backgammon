package de.htwg.se.backgammon.metric.api
import play.api.libs.json.{Reads, Json, Writes}

case class GetStatsRequest(playerNames: Seq[String])

object GetStatsRequest {
  implicit val reads: Reads[GetStatsRequest] = Json.reads[GetStatsRequest]
  implicit val writes: Writes[GetStatsRequest] = Json.writes[GetStatsRequest]
}