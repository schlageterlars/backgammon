package de.htwg.se.backgammon.view.component

import scalafx.scene.image.ImageView
import scalafx.scene.image.Image
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import scala.io.Source
import javafx.scene.input.MouseEvent
import scalafx.animation.Timeline
import scalafx.animation.KeyFrame
import scalafx.util.Duration

import scala.concurrent.duration._
import java.util.concurrent._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.ScheduledThreadPoolExecutor
import scalafx.beans.binding.BooleanBinding

val DICE_SIZE = 40

class Die(
    var _dots: Int,
    val boardX: Double,
    val boardY: Double,
    val boardSize: Size
) extends ImageView {

  def dots = _dots
  def dots_=(d: Int) = {
    image = getImageByDots(d)
    _dots = d
  }

  {
    image = getImageByDots(_dots)
    fitHeight = DICE_SIZE
    fitWidth = DICE_SIZE

    randomizePosition
  }

  def roll(number: Int, changePosition: Boolean = true) = {
    if changePosition then randomizePosition

    val ex = new ScheduledThreadPoolExecutor(1)
    var future: ScheduledFuture[_] = null
    val task: Runnable = new Runnable {
      val repetitions = 10
      var done = 0
      def run(): Unit = {
        dots = random

        done = done + 1
        if (done >= repetitions) {
          dots = number
          future.cancel(true)
        }
      }
    }

    future = ex.scheduleAtFixedRate(task, 1, 100, TimeUnit.MILLISECONDS)
  }

  def random = {
    var random = Random.between(1, 7)
    while (random == dots) {
      random = Random.between(1, 7)
    }; random
  }

  val padding = 100

  def boardMiddleY = boardY + (boardSize.height / 2) - DICE_SIZE / 2

  def randomizePosition = {
    x = boardX + padding + Random.between(0, boardSize.width - (padding * 2))
    y = boardMiddleY + Random.between(-20, 20)
    rotate = Random.between(-25, 25)
  }

  def overlaps(die: Die): Boolean = {
    (x < (die.x + die.fitWidth) &&
    (x + fitWidth) > die.x &&
    y < (die.y + die.fitHeight) &&
    (y + fitHeight) > die.y).get()
  }

  private def getImageByDots(dots: Int) = new Image(
    s"file:src/main/resources/$dots.png"
  )

  private def randomImage: Image = getImageByDots(Random.between(1, 7))

  override def toString: String = s"$_dots"
}
