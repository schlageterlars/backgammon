package de.htwg.se.backgammon.model

import scala.xml.Elem
import de.htwg.se.backgammon.model.storage.Storable
import scala.xml.{Elem, Null, TopScope, Text}


trait IModel extends Storable {

  def next: Player

  def doublets: Boolean

  var dice: List[Int]

  def roll: List[Int]

  def game_=(game: IGame): Unit

  def game: IGame

  var movesThisRound: List[IGame]

  def previousGame: IGame

  def player: Player

  override def asXml: Elem = {
  val currentElem: Elem = Elem(null, "current", Null, TopScope, minimizeEmpty = true, Text(player.toString()))

  val dieElems: Seq[Elem] = dice.map { value =>
    Elem(null, "die", Null, TopScope, minimizeEmpty = true, Text(value.toString))
  }
  val diceElem: Elem = Elem(null, "dice", Null, TopScope, minimizeEmpty = true, dieElems*)

  val doubletsElem: Elem = Elem(null, "doublets", Null, TopScope, minimizeEmpty = true, Text(doublets.toString()))

    Elem(null, "data", Null, TopScope, minimizeEmpty = true,
    currentElem,
    diceElem,
    doubletsElem,
    game.asXml  
  )
}
}
