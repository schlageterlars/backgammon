package de.htwg.se.backgammon.view.component.configuration

import de.htwg.se.backgammon.view.component.Size

trait Configuration {
  def colors: ColorPalette
}

trait FrameConfiguration extends Configuration {
  def size: Size
  def height: Double = size.height
  def width: Double = size.width
}

trait BoardConfiguration extends Configuration {
  def size: Size
  def height: Double = size.height
  def width: Double = size.width

  def frameSize: Size
  def lineWidth: Int

  def xCoord = (frameSize.width - size.width) / 2
  def yCoord = (frameSize.height - size.height) / 2
}

trait PlayerConfiguration extends Configuration {
  def frameSize: Size

  def marginTop: Double
  def marginRight: Double
  def width: Double
  def cornerRadius: Double

  def mid = frameSize.width / 2 - width / 2
  def xCoord = frameSize.width - marginRight - width
  def yCoordWhite = frameSize.height - marginTop - width
}

trait PointConfiguration extends Configuration {
  def boardSize: Size
  def height: Double
  def margin: Double
}
