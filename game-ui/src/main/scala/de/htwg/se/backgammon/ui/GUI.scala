package de.htwg.se.backgammon.ui

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.application.JFXApp3
import scalafx.scene.shape.Rectangle
import scalafx.beans.property.StringProperty
import scalafx.scene.layout.HBox
import scalafx.geometry.Insets
import scalafx.scene.text.Text
import scalafx.scene.paint.LinearGradient
import scalafx.scene.paint.Stops
import scalafx.scene.paint.Color._
import scalafx.scene.effect.DropShadow

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label, ScrollPane}
import scalafx.scene.layout.{Priority, VBox}
import scalafx.scene.web.HTMLEditor
import scalafx.scene.canvas.GraphicsContext
import de.htwg.se.backgammon.ui.component.Board
import de.htwg.se.backgammon.ui.component.Size
import de.htwg.se.backgammon.core.IGame
import scalafx.scene.layout.Pane
import scalafx.scene.input.MouseEvent
import scalafx.scene.shape.Circle
import de.htwg.se.backgammon.ui.component.Checker
import de.htwg.se.backgammon.ui.component.util.DraggedChecker
import scalafx.application.HostServices
import de.htwg.se.backgammon.ui.component.*
import de.htwg.se.backgammon.ui.component.PlayerState
import scalafx.scene.shape.DrawMode
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.IMove
import de.htwg.se.backgammon.util.Command
import de.htwg.se.backgammon.exception.MoveException
import de.htwg.se.backgammon.core.base.DefinedMove
import scalafx.scene.PerspectiveCamera
import scala.util.Try
import scala.util.Random
import scalafx.application.Platform
import component.Dice
import de.htwg.se.backgammon.ui.util.PrettyPrint.PrintBold
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.ui.component.configuration.Default.{given}
import javafx.animation.AnimationTimer
import de.htwg.se.backgammon.ui.component.util.WinAnimation
import de.htwg.se.backgammon.core.base.BearOffMove
import scalafx.event.ActionEvent
import de.htwg.se.backgammon.core.Input.Skip
import de.htwg.se.backgammon.core.IController
import de.htwg.se.backgammon.core.Observer
import de.htwg.se.backgammon.core.Event

class GUI(controller: IController) extends JFXApp3 with Observer {
  controller.add(this)

  var pane: Pane = null
  var draggedChecker: DraggedChecker = DraggedChecker.empty
  var winAnimation: WinAnimation = null

  val board: Board = Board()
  val playerState: PlayerState = PlayerState()
  val dice: Dice = Dice()

  val bars: Bars = Bars.createDefault()
  var skipButton: Button = null;

  var onStart: () => Unit = () => ()

  override def update(event: Event, exception: Option[Throwable]): Unit = {
    Platform.runLater(onEvent(event, exception))
  }

  def onEvent(event: Event, exception: Option[Throwable]) = event match {
    case Event.Move          => onMove(controller.game)
    case Event.PlayerChanged => onPlayerChanged(controller.currentPlayer)
    case Event.DiceRolled    => dice.roll(controller.dice)
    case Event.InvalidMove =>
      println(exception.getOrElse(MoveException()).getMessage())
      draggedChecker.reset()
    case Event.GameOver => onGameOver(controller.game.winner.get)
    case _              =>
  }

  def onGameOver(winner: Player) = {
    winAnimation.start
    playerState.set(winner, "🏆")
    stage.title = s"Backgammon - Congratulation to $winner!!"
  }

  def onMove(game: IGame) = {
    board.setGame(game); bars.setGame(game)
    draggedChecker.reset()
    skipButton.visible = !controller.existsPossibleMoves
  }

  def onPlayerChanged(current: Player) = {
    playerState.set(current)
    board.activateCheckers(current)
    stage.title = s"Backgammon - $current it's your turn!"
  }

  override def start(): Unit = {
    onStart()
    try {
      stage = new JFXApp3.PrimaryStage {
        title = "Backgammon"
        resizable = false
        scene = new Scene(
          given_FrameConfiguration.width,
          given_FrameConfiguration.height
        ) {
          // — all of your existing pane, event handlers, etc. —
          pane = new Pane {
            board.setGame(controller.game)
            dice.create(2)
            val canvas = new Canvas(
              given_FrameConfiguration.width,
              given_FrameConfiguration.height
            )
            winAnimation = WinAnimation(canvas)
            skipButton = SkipButton()
            skipButton.onAction = (_: ActionEvent) => {
              controller.skip(controller.die)
              skipButton.visible = !controller.existsPossibleMoves
            }
            children = Seq(
              board,
              playerState,
              bars,
              dice,
              canvas,
              skipButton
            )
          }

          onMouseClicked = (event: MouseEvent) => {
            board.handleMouseEvent(event, onClicked = onBoardClicked)
            bars.handleMouseEvent(event, onClicked = onBoardClicked)
          }

          onMouseMoved = (event: MouseEvent) => {
            board.handleMouseEvent(event, doHovering = doHovering)
            bars.handleMouseEvent(event, doHovering = doHovering)
            if (draggedChecker.isDefined) then draggedChecker.move(event)
          }

          onShown = () => {
            dice.roll(controller.dice)
            bars.setGame(controller.game)
            onPlayerChanged(controller.currentPlayer)
            skipButton.visible = !controller.existsPossibleMoves
          }

          content = pane
        }
      }
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        javafx.application.Platform.exit()
    }
}
  def doHovering(element: GUIElement): Boolean = element match {
    case point: Point     => draggedChecker.isDefined
    case checker: Checker => draggedChecker.isEmpty
    case _                => true
  }

  def onBoardClicked(element: GUIElement): Unit = element match {
    case point: Point if draggedChecker.isDefined => {
      val from = board.indexOf(draggedChecker.point)
      val to = board.indexOf(point)

      controller.doAndPublish(
        controller.put,
        Move.create(controller.game, controller.currentPlayer, from, to)
      )

      pane.children.remove(this.draggedChecker)
    }
    case checker: Checker
        if draggedChecker.isEmpty
          && checker.player == controller.currentPlayer => {
      canBearOff(checker) match {
        case Some(move: BearOffMove) =>
          controller.doAndPublish(controller.put, move)
        case _ => {
          draggedChecker = new DraggedChecker(checker)
          pane.children.add(draggedChecker)
        }
      }
    }
    case _ =>
  }

  def canBearOff(checker: Checker): Option[BearOffMove] = {
    if (!controller.hasToBearOff) then None
    else {
      val from = board.indexOf(checker.point)
      if (from == -1) then None
      else {
        val moves = controller.dice.map(die => BearOffMove(from, die))
        moves.find(m => m.isValid(controller.game))
      }
    }
  }
}
