package de.htwg.se.backgammon.view.component.configuration

import scalafx.scene.paint.Color

trait ColorPalette {

  // board colors
  def boardBackground: Color
  def boardGrid: Color

  // field colors
  def field: Color
  def field_1: Color

  // checkers colors
  def checkersWhite: Color
  def checkersBlack: Color
}

object ColorPalette {
  def toRGBA(color: Color) =
    s"rgba(${(color.getRed * 255).toInt}, ${(color.getGreen * 255).toInt}, " +
      s"${(color.getBlue * 255).toInt}, ${(color.getOpacity * 255).toInt})"
}

class PrimaryColorPalette extends ColorPalette {

  // board colors
  val boardBackground = Color.rgb(192, 223, 161)
  val boardGrid = Color.rgb(159, 196, 144)

  // field colors
  val field = Color.rgb(130, 163, 161)
  val field_1 = Color.rgb(70, 83, 98)

  // checkers colors
  val checkersWhite = Color.WHITESMOKE
  val checkersBlack = Color.rgb(188, 138, 95)
}

class SecondaryColorPalette extends ColorPalette {
  // board colors
  override val boardBackground = Color.rgb(253, 244, 205)
  override val boardGrid = Color.rgb(112, 90, 4)

  // field colors
  override val field = Color.rgb(249, 217, 90)
  override val field_1 = Color.rgb(217, 175, 8)

  // checkers colors
  override val checkersWhite = Color.rgb(138, 199, 215)
  override val checkersBlack = Color.rgb(246, 115, 89)
}
