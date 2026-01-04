package de.htwg.se.backgammon.model

import scala.xml.Elem
import de.htwg.se.backgammon.model.storage.Storable

trait IModel extends Storable {

  def next: Player

  var dice: List[Int]

  def roll: List[Int]

  def game_=(game: IGame): Unit

  def game: IGame

  var movesThisRound: List[IGame]

  def previousGame: IGame

  def player: Player

  override def asXml: Elem = {
    <data> 
        <current>{player}</current>
        <dice>
            {dice.map(value => <die>{value}</die>)}
        </dice>
        {game.asXml}
    </data>
  }
}
