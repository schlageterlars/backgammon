package de.htwg.se.backgammon.ui.component

import scalafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import de.htwg.se.backgammon.core.IField
import de.htwg.se.backgammon.core.base.Field
import de.htwg.se.backgammon.core.Player
import scalafx.scene.effect.DropShadow
import scalafx.Includes.jfxColor2sfx
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Polygon
import scalafx.scene.layout.Pane
import scalafx.Includes.eventClosureWrapperWithParam
import javafx.scene.input.MouseEvent
import scalafx.scene.Group
import de.htwg.se.backgammon.ui.component.configuration.ColorPalette

val MARGIN_BOTTOM = 5
val PADDING = 2
val CHECKER_SIZE = 34

class Point(
    x: Array[Double],
    y: Array[Double],
    size: Size,
    color: Color,
    colorPalette: ColorPalette
) extends Pane
    with GUIElement {
  private var field: IField = Field()
  private var checkers: GUIList[Checker] = GUIList()
  def getCheckers() = checkers.asList

  private def xCoord = x(0)
  private def yCoord = y(0) - (if isUpsideDown then 0 else size.height)
  private var polygon: Polygon = null

  {
    val rectangle = new Rectangle {
      width = size.width
      height = size.height
      fill = Color.TRANSPARENT
      this.setX(xCoord)
      this.setY(yCoord)
    }

    polygon = new Polygon {
      points.addAll(x(0), y(0), x(1), y(1), x(2), y(2))
      fill = color
    }

    children = Seq(polygon, rectangle, checkers)
  }

  override def onHovering() = {
    polygon.effect = DropShadow(5.0, Color.BLACK)
  }

  override def onMouseExit() = {
    polygon.effect = null
  }

  override def isMouseInside(e: MouseEvent) = {
    e.getX() >= xCoord && e.getX() <= (xCoord + size.width) &&
    e.getY() >= yCoord && e.getY() <= (yCoord + size.height)
  }

  def set(field: IField) = {
    draw(this.field, field)
    this.field = field
  }

  def activate(player: Player) = {
    checkers.asList.foreach(c => c.activated = (player == field.occupier))
  }

  def draw(previous: IField, field: IField): Unit = {
    if (
      (previous.number == field.number &&
      previous.occupier == field.occupier)
    ) then return

    checkers.clear()
    if (field.number == 0) then return

    for (i <- (field.number - 1) to 0 by -1) {
      createChecker(field.occupier, i)
    }
  }

  def createChecker(player: Player, postion: Int) = {
    checkers.add(
      Checker(
        colorPalette,
        player,
        x(2),
        getCheckerY(postion),
        false,
        this
      )
    )
  }

  def getCheckerY(index: Int) = {
    getCheckerBaseY + index * (if isUpsideDown then CHECKER_SIZE + PADDING
                               else -(CHECKER_SIZE + PADDING))
  }

  def getCheckerBaseY = {
    CHECKER_RADIUS + (if isUpsideDown then y(0) + MARGIN_BOTTOM
                      else y(0) - CHECKER_SIZE - MARGIN_BOTTOM)
  }

  def isUpsideDown = y(0) < y(2)

  override def toString(): String =
    s"Point (${x.mkString(",")}/${y.mkString(",")})"

  override def childElements = checkers.asList
}
