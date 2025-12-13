package de.htwg.se.backgammon.model.base

import scala.util.Random
import scala.util.Try
import scala.util.Success
import java.util.Map.Entry
import scala.util.Failure
import org.scalactic.Fail
import de.htwg.se.backgammon.model.strategy.ValidateBearOffMoveStrategy
import de.htwg.se.backgammon.model.IGame
import de.htwg.se.backgammon.model.IField
import de.htwg.se.backgammon.model.IMove
import de.htwg.se.backgammon.model.Player
import de.htwg.se.backgammon.model.strategy.ValidateBearInMoveStrategy
import de.htwg.se.backgammon.model.strategy.DefaultValidateMoveStrategy
import de.htwg.se.backgammon.model.strategy.MoveStrategy
import scala.xml.Elem
import scala.xml.Node

case class Game(fields: List[IField], barWhite: Int = 0, barBlack: Int = 0)
    extends IGame
    with GameSeq(fields) {

  def this(fields: Int, pieces: Int) =
    this(Game.create(DefaultSetup(fields, pieces)))

  def this(setup: Setup) = this(Game.create(setup))

  def move(move: IMove): Try[IGame] = {
    val to = move.whereToGo(this)
    val validateStrategy = move match {
      case move: BearInMove =>
        ValidateBearInMoveStrategy(this, move.player, to)
      case _: BearOffMove    => ValidateBearOffMoveStrategy(this, move.from, to)
      case Move(from, steps) => DefaultValidateMoveStrategy(this, from, to)
    }
    validateStrategy.execute() match {
      case Failure(ex) => Failure(ex)
      case _           => Success(MoveStrategy(this, move, to).execute())
    }
  }

  def get(position: Int) = fields(position)

  def winner: Option[Player] =
    if !fields.exists(_.occupier == Player.White) then Some(Player.White)
    else if !fields.exists(_.occupier == Player.Black) then Some(Player.Black)
    else Option.empty

  def homeBoard = Map(
    Player.White -> fields.drop((fields.length / 4) * 3),
    Player.Black -> fields.dropRight((fields.length / 4) * 3)
  )

  def numberOfPieces = Map(
    Player.White -> fields.filter(_.occupier == Player.White).map(_.number).sum,
    Player.Black -> fields.filter(_.occupier == Player.Black).map(_.number).sum
  )

  def bar = Map(Player.White -> barWhite, Player.Black -> barBlack)

  override def toString: String =
    s"$barWhite : ${fields.mkString(" ")} : $barBlack"

  def ==(that: IGame): Boolean = fields.equals(that.fields)

  def !=(that: IGame): Boolean = !(this == that)

}

object Game {
  def create(
      setup: Setup
  ): List[Field] = {
    val side = List.tabulate(setup.fields / 2)(index =>
      Field(setup.toMap.getOrElse(index, 0))
    )
    side ++ side.map(f => Field(-f.pieces)).reverse
  }

  def fromXml(xml: Node) = {
    val barWhite = (xml \ "barWhite").text.toInt
    val barBlack = (xml \ "barBlack").text.toInt
    val fields =
      (xml \ "fields" \ "field").map(node => Field(node.text.toInt)).toList

    new Game(fields, barWhite, barBlack)
  }
}

trait GameSeq(private val fields: List[IField]) extends IndexedSeq[IField] {
  override def apply(i: Int): IField = fields(i)
  override def length: Int = fields.length
}
