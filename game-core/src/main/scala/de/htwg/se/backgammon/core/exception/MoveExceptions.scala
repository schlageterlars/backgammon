package de.htwg.se.backgammon.exception

import de.htwg.se.backgammon.model.Player

class MoveException extends Exception {
  override def getMessage() = "Unknown error has occurred."
}

case class NoMoveException() extends MoveException {
  override def getMessage() = "There is no move to redo!"
}

case class EmptyFieldException(val position: Int) extends MoveException {
  override def getMessage() = s"Field $position is empty!"
}

case class BarIsNotEmptyException() extends MoveException {
  override def getMessage() = s"You can only bear in!"
}

case class AttackNotPossibleException(from: Int, to: Int, pieces: Int)
    extends MoveException {
  override def getMessage() =
    s"Different players on fields $from and $to {$pieces pieces on field}."
}

case class NotYourFieldException(from: Int, player: Player, other: Player)
    extends MoveException {
  override def getMessage() = s"You are $player, but $other is on field $from."
}

case class WrongDirectionException(val player: Player) extends MoveException {
  override def getMessage() =
    s"It's not possible to move to the $wrongDirection, because you are $player."

  def wrongDirection = if (player == Player.White) "left" else "right"
}

case class DieNotExistException(val steps: Int, val dice: List[Int])
    extends MoveException {
  override def getMessage() =
    s"You can't move $steps steps. You rolled${dice.map(die => s" a $die").mkString(" and")}"
}

case class FieldDoesNotExistException(from: Int, steps: Int, to: Int)
    extends MoveException {
  override def getMessage() =
    s"You can't move $steps steps from $from, because field $to doesn't exist."
}
