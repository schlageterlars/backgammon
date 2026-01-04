package de.htwg.se.backgammon.model.base

import de.htwg.se.backgammon.model.Player
import de.htwg.se.backgammon.model.IModel
import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.IDice
import scala.xml.Elem
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import scala.xml.Node

val MOVES_PER_ROUND = 2

case class Model(
    private var _game: IGame,
    var player: Player,
    val diceStrategy: IDice = new Dice(),
    var dice: List[Int] = List()
) extends IModel {
  def this(game: Game, diceStrategy: IDice) =
    this(game, Player.White, diceStrategy)

  if (dice.isEmpty) roll

  def next = {
    if player == Player.White then player = Player.Black
    else player = Player.White
    movesThisRound = Nil; player
  }

  def roll = {
    dice = diceStrategy.roll(MOVES_PER_ROUND)
    val doublets: Boolean = dice match {
      case List(a, b) => a == b
      case _ => false
    }
    if doublets then
      dice = List.fill(4)(dice.head)
      
    dice
  }

  var previousGame = _game

  def game_=(game: IGame) = {
    previousGame = _game
    _game = game
    movesThisRound = movesThisRound.::(game)
  }

  def game = _game

  var movesThisRound: List[IGame] = Nil
}

object Model {

  implicit val modelReads: Reads[Model] = Json.reads[Model]
  implicit val modelWrites: Writes[Model] = Json.writes[Model]
  def fromJson(json: JsValue) = {
    json.asOpt[Model] match
      case Some(model: Model) => model
      case _ =>
        throw new IllegalArgumentException(
          s"Json can't be converted to a game!"
        )
  }

  def fromXml(xml: Node): Model = {
    val game = Game.fromXml((xml \ "game")(0))
    val player = Player.withName((xml \ "current").text)
    val dice =
      (xml \ "dice" \ "die").map(node => node.text.toInt).toList

    val model = new Model(game, player, new Dice(), dice)
    model
  }
}
