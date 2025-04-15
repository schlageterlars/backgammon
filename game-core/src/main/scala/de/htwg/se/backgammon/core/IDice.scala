package de.htwg.se.backgammon.model

import play.api.libs.json.Reads
import de.htwg.se.backgammon.model.base.Dice
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.json.JsResult
import play.api.libs.json.Format
import play.api.libs.json.JsNull
import play.api.libs.json.JsSuccess

trait IDice {
  def roll: Int
  def roll(times: Int): List[Int]
}

object IDice {
    implicit val format: Format[IDice] = new Format[IDice]{

      override def writes(o: IDice): JsValue = JsNull

      override def reads(json: JsValue): JsResult[IDice] = JsSuccess(new Dice())
    }
}