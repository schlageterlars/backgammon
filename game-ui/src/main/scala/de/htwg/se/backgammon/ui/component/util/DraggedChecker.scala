package de.htwg.se.backgammon.ui.component.util

import de.htwg.se.backgammon.ui.component.Checker
import de.htwg.se.backgammon.core.Player
import javafx.scene.input.MouseEvent
import scalafx.scene.input.KeyCode.Play
import de.htwg.se.backgammon.ui.component.configuration.PrimaryColorPalette

case class DraggedChecker(var original: Checker)
    extends Checker(
      original.colors,
      original.player,
      original.xCoord,
      original.yCoord,
      original.activated,
      point = original.point
    ) {
  {
    if player != Player.None then original.setVisible(false)
  }

  def reset() = {
    original.setVisible(true)
    player = Player.None
  }

  def isDefined = player != Player.None

  def isEmpty = player == Player.None
}

object DraggedChecker {
  def empty =
    new DraggedChecker(
      Checker(PrimaryColorPalette(), Player.None, 0, 0, false, null)
    )
}
