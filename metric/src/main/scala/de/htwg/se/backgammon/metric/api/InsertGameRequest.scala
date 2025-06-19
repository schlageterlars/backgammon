package de.htwg.se.backgammon.metric.api

package de.htwg.se.backgammon.metric.api
import play.api.libs.json.{Reads, Json, Writes}

case class InsertGameRequest(timestamp: Long, playerName: String)

object InsertGameRequest {
  implicit val reads: Reads[InsertGameRequest] = Json.reads[InsertGameRequest]
  implicit val writes: Writes[InsertGameRequest] = Json.writes[InsertGameRequest]
}