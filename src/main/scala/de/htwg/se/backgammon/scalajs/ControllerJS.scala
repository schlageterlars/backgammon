package de.htwg.se.backgammon.scalajs

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._ 
import scalajs.js.JSConverters.JSRichIterableOnce
import de.htwg.se.backgammon.controller.IController
import de.htwg.se.backgammon.controller.base.Controller
import de.htwg.se.backgammon.model.base.DefaultSetup
import de.htwg.se.backgammon.model.base.Dice
import de.htwg.se.backgammon.model.base.Game
import de.htwg.se.backgammon.model.base.Model
import de.htwg.se.backgammon.model.IMove

@JSExportTopLevel("ControllerJS")
object ControllerJS {
  var controller: IController = _

  @JSExport
  def init(): Unit = {
    val model = new Model(
      new Game(DefaultSetup(24, 12)),
      new Dice()
    )
    controller = Controller(model)
  }

  @JSExport
  def getFields(): js.Array[Int] = controller.data.game.fields.map(f => f.pieces).toJSArray

  @JSExport
  def getBarWhite(): Int = controller.data.game.barWhite

  @JSExport
  def getBarBlack(): Int = controller.data.game.barBlack

  @JSExport
  def getCurrentPlayer(): String = controller.currentPlayer.toString

  @JSExport
  def getDice(): js.Array[Int] = controller.dice.toJSArray

  @JSExport
  def doMove(from: Int, to: Int): Unit = {
    val move = de.htwg.se.backgammon.model.base.Move.create(
              controller.game, controller.currentPlayer, from.toInt, to.toInt
    ) 
    controller.doAndPublish(controller.put, move)
  }
}

