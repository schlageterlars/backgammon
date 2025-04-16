package de.htwg.se.backgammon.view.component.util

import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color
import de.htwg.se.backgammon.view.component.configuration.FrameConfiguration
import scalafx.scene.canvas.Canvas
import javafx.animation.AnimationTimer

class Confetti(val canvas: Canvas) {
  var x = math.random * canvas.getWidth()
  var y = math.random * canvas.getHeight()

  val size = 10
  val speed = 5

  def display(gc: GraphicsContext): Unit = {
    gc.fill = Color.rgb(
      (math.random * 255).toInt,
      (math.random * 255).toInt,
      (math.random * 255).toInt
    )
    gc.fillOval(x, y, size, size)
  }

  def fall(): Unit = {
    y += speed

    if (y > canvas.getHeight()) {
      y = 0
      x = math.random * canvas.getWidth()
    }
  }
}

class WinAnimation(canvas: Canvas) {
  val confetti = Seq.fill(100)(
    new Confetti(canvas)
  )

  def start = new AnimationTimer {
    val gc = canvas.graphicsContext2D

    override def handle(now: Long): Unit = {
      gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight())

      for (k <- confetti) {
        k.display(gc)
        k.fall()
      }
    }
  }.start()
}
