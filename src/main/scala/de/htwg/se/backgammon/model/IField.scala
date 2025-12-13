package de.htwg.se.backgammon.model

import de.htwg.se.backgammon.model.base.Field

trait IField {

  def +(that: Int): IField

  def -(that: Int): IField

  def isOccupied(): Boolean

  def isEmpty(): Boolean

  def hasSameOccupierAs(that: IField): Boolean

  def hasDifferentOccupierThen(that: IField): Boolean

  def isOccupiedBy(player: Player): Boolean

  def isNotOccupiedBy(player: Player): Boolean

  def copy(number: Int): IField

  def number: Int

  def occupier: Player

  def pieces: Int

}
