package de.htwg.se.backgammon.ui.component

import scalafx.scene.layout.Pane
import de.htwg.se.backgammon.core.Player
import scalafx.scene.shape.Ellipse
import scalafx.scene.effect.DropShadow
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import javafx.scene.input.MouseEvent
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.scene.Group
import scala.collection.mutable.ListBuffer
import de.htwg.se.backgammon.ui.GUI
import de.htwg.se.backgammon.ui.component.configuration.ColorPalette

val CHECKER_RADIUS = 17

class Checker(
    val colors: ColorPalette,
    var player: Player,
    val xCoord: Double,
    val yCoord: Double,
    var activated: Boolean = false,
    val point: Point
) extends Circle
    with GUIElement {
  {
    centerX = xCoord
    centerY = yCoord
    radius = CHECKER_RADIUS
    fill = player match {
      case Player.White => colors.checkersWhite
      case _            => colors.checkersBlack
    }
    effect = new DropShadow(3.0, Color.BLACK)
  }

  override def onHovering() = {
    effect =
      if activated then new DropShadow(5.0, Color.BLACK)
      else new DropShadow(3.0, Color.BLACK)
  }

  override def isMouseInside(e: MouseEvent): Boolean = {
    e.getX() >= (xCoord - CHECKER_RADIUS) &&
    e.getX() <= (xCoord + CHECKER_RADIUS) &&
    e.getY() >= (yCoord - CHECKER_RADIUS) &&
    e.getY() <= (yCoord + CHECKER_RADIUS)
  }

  override def onMouseExit(): Unit = {
    effect = new DropShadow(3.0, Color.BLACK)
  }

  override def move(e: MouseEvent, offsetX: Double, offsetY: Double) = {
    centerX = e.sceneX - offsetX
    centerY = e.sceneY - offsetY
  }

  override def offsetX(event: MouseEvent): Double =
    (event.sceneX - centerX.toDouble)

  override def offsetY(event: MouseEvent): Double =
    (event.sceneY - centerY.toDouble)

  override def isDraggable = true

  override def clone(): Checker =
    new Checker(colors, player, xCoord, yCoord, activated, point)

}
