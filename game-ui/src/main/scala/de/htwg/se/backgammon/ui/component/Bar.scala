package de.htwg.se.backgammon.view.component

import scalafx.scene.Group
import de.htwg.se.backgammon.core.Player
import scalafx.scene.shape.Rectangle
import scalafx.scene.effect.GaussianBlur
import scalafx.scene.paint.Color
import scalafx.scene.input.MouseEvent
import de.htwg.se.backgammon.view.component.configuration.ColorPalette

case class Bar(
    colors: ColorPalette,
    player: Player,
    xCoord: Double,
    yCoord: Double
) extends Group {
  private val checkers: GUIList[Checker] = GUIList()
  private val background = Background()

  {
    children = Seq(checkers)
  }

  def createChecker(position: Int) = {
    checkers.add(
      Checker(
        colors,
        player,
        getX(position),
        yCoord,
        false,
        null
      )
    )
  }

  def getX(position: Double) = xCoord - (position * (CHECKER_SIZE + PADDING))

  def set(checkers: Int) = {
    this.checkers.clear()
    if (checkers > 0) {
      (0 to checkers - 1).foreach(position => createChecker(position))
    }
  }

  def handleMouseEvent(
      event: MouseEvent,
      doHovering: GUIElement => Boolean = ((e) => true),
      onClicked: GUIElement => Unit = (e => None)
  ) = checkers.foreach(c => c.handleMouseEvent(event, doHovering, onClicked))

  private class Background extends Rectangle {
    private val BACKGROUND_PADDING = 5

    update
    def update = {
      fill = Color.GREY
      x = xCoord - CHECKER_SIZE / 2 - BACKGROUND_PADDING
      y = yCoord - PLAYER_BOX_WIDTH / 2
      width = (CHECKER_SIZE * checkers.length) + BACKGROUND_PADDING * 2
      height = PLAYER_BOX_WIDTH
      arcWidth = 20
      arcHeight = 20
      effect = new GaussianBlur(10)
    }
  }

}
