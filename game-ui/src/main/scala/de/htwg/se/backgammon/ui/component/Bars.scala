package de.htwg.se.backgammon.ui.component

import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.Player
import scalafx.scene.input.MouseEvent
import de.htwg.se.backgammon.ui.component.configuration.ColorPalette
import de.htwg.se.backgammon.ui.component.configuration.FrameConfiguration

val MARGIN_RIGHT = 25

class Bars(bars: Bar*) extends GUIList[Bar] {
  {
    bars.foreach(add(_))
  }

  def setGame(game: IGame) = {
    asList.foreach(bar => bar.set(game.bar(bar.player)))
  }

  def handleMouseEvent(
      event: MouseEvent,
      doHovering: GUIElement => Boolean = ((e) => true),
      onClicked: GUIElement => Unit = (e => None)
  ) = bars.foreach(p => p.handleMouseEvent(event, doHovering, onClicked))
}

object Bars {
  def createDefault()(using frame: FrameConfiguration) = {
    val y = MARGIN_TOP + PLAYER_BOX_WIDTH / 2
    val x = frame.width - PLAYER_BOX_WIDTH - CHECKER_SIZE - MARGIN_RIGHT
    Bars(
      Bar(frame.colors, Player.Black, x, y),
      Bar(frame.colors, Player.White, x, frame.height - y)
    )
  }
}
