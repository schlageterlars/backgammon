package de.htwg.se.backgammon.view.component.configuration

import de.htwg.se.backgammon.view.component.Size

object Default {
  given ColorPalette = PrimaryColorPalette()
  given FrameConfiguration = DefaultFrameConfiguration()
  given BoardConfiguration = DefaultBoardConfiguration()
  given PointConfiguration = DefaultPointConfiguration()
  given PlayerConfiguration = DefaultPlayerConfiguration()
}

case class DefaultPlayerConfiguration(
    frameSize: Size = Default.given_FrameConfiguration.size,
    colors: ColorPalette = Default.given_ColorPalette
) extends PlayerConfiguration {
  val marginTop = 25
  val marginRight = 25
  val width = 50
  val cornerRadius = 5
}

case class DefaultPointConfiguration(
    boardSize: Size = Default.given_BoardConfiguration.size,
    colors: ColorPalette = Default.given_ColorPalette
) extends PointConfiguration {
  val height = (boardSize.height / 2) * 0.9
  val margin = 5
}

case class DefaultBoardConfiguration(
    frameSize: Size = Default.given_FrameConfiguration.size,
    colors: ColorPalette = Default.given_ColorPalette
) extends BoardConfiguration {
  val size = Size(800, 500)
  val lineWidth = 4
}

case class DefaultFrameConfiguration(
    colors: ColorPalette = Default.given_ColorPalette
) extends FrameConfiguration{
  val size = Size(900, 700)
}
