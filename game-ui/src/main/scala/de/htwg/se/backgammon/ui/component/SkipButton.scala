package de.htwg.se.backgammon.view.component

import scalafx.scene.control.Button
import de.htwg.se.backgammon.view.component.configuration.FrameConfiguration
import de.htwg.se.backgammon.view.component.configuration.ColorPalette

class SkipButton(using conf: FrameConfiguration) extends Button {
  prefWidth = 100
  prefHeight = 20
  layoutY = conf.height - 80
  layoutX = conf.width / 2 - (100 / 2)
  text = "skip the turn"
  visible = false
  style =
    s"-fx-background-color: ${ColorPalette.toRGBA(conf.colors.boardGrid)};" +
      "-fx-text-fill: white;" +
      "-fx-cursor: hand;"
}
