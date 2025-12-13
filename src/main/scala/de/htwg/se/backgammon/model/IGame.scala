package de.htwg.se.backgammon.model

import scala.util.Try
import de.htwg.se.backgammon.model.base.Game

trait IGame extends IndexedSeq[IField] {
  def fields: List[IField]
  def barWhite: Int
  def barBlack: Int

  def move(move: IMove): Try[IGame]

  def get(position: Int): IField

  def winner: Option[Player]

  def homeBoard: Map[Player, List[IField]]

  def numberOfPieces: Map[Player, Int]

  def bar: Map[Player, Int]

  def ==(that: IGame): Boolean

  def !=(that: IGame): Boolean
}
