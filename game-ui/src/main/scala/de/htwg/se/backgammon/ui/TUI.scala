package de.htwg.se.backgammon.ui

import scala.util.Try
import de.htwg.se.backgammon.exception.MoveException
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.base.BearInMove
import de.htwg.se.backgammon.ui.util.PrettyPrint.MarkDifferencesBetweenGames
import de.htwg.se.backgammon.ui.util.PrettyPrint.PrintDiceResults
import de.htwg.se.backgammon.ui.util.PrettyPrint.PrintBold
import de.htwg.se.backgammon.ui.util.PrettyPrint.printNew
import de.htwg.se.backgammon.ui.util.PrettyPrint.printGameWithIndizies
import de.htwg.se.backgammon.core.Input
import de.htwg.se.backgammon.core.Quit
import de.htwg.se.backgammon.core.Undo
import de.htwg.se.backgammon.core.Redo
import de.htwg.se.backgammon.core.base.BearOffMove
import de.htwg.se.backgammon.core.Skip
import de.htwg.se.backgammon.ui.util.PrettyPrint.printGameOver
import de.htwg.se.backgammon.core.IController
import de.htwg.se.backgammon.core.Observer
import de.htwg.se.backgammon.core.Event

class TUI(controller: IController) extends Observer:
  controller.add(this)
  var continue = true
  def run =
    printNew(controller.game); update(Event.DiceRolled)
    inputLoop()

  override def update(e: Event, ex: Option[Throwable] = None) =
    e match
      case Event.Quit => continue = false
      case Event.Move =>
        printNew(controller.game markDifferencesTo controller.previousGame)
      case Event.InvalidMove =>
        printErr(s"Not possible! ${ex.getOrElse(MoveException()).getMessage()}")
      case Event.PlayerChanged =>
        println(s"${s"${controller.currentPlayer}".bold} it's your turn!")
      case Event.DiceRolled =>
        println(
          s"You rolled the dice twice: ${controller.dice.toPrettyString}"
        )
      case Event.BarIsNotEmpty =>
        println(
          "It is at least one checker in your bar, which die do you wanna use?"
        )
      case Event.AllCheckersInTheHomeBoard =>
        println(
          "All of your checkers are on your homeboard, so you can bear off."
        )
      case Event.GameOver => printGameOver(controller.game.winner.get)

  def inputLoop(): Unit =
    analyseInput(readLine) match
      case None if controller.checkersInBar =>
        printErr(s"Use: <steps from ${playerColor.bold} bar>")
      case None =>
        printErr(s"Use: <field with ${playerColor.bold} checkers>")
      case Some(move: Move) => controller.doAndPublish(controller.put, move)
      case Some(_: Redo)    => controller.doAndPublish(controller.redo)
      case Some(_: Undo)    => controller.undoAndPublish(controller.undo)
      case Some(_: Skip)    => controller.skip(controller.die)
      case Some(_)          => controller.quit

    if continue then inputLoop()

  def analyseInput(input: String): Option[Input] =
    input match
      case "q"    => Input.Quit
      case "undo" => Input.Undo
      case "redo" => Input.Redo
      case "skip" => Input.Skip
      case string => {
        Try({
          val input = Integer.parseInt(string)
          Some(createMove(input))
        }).getOrElse(None)
      }

  def createMove(from: Int) = {
    if (controller.checkersInBar) then
      BearInMove(controller.currentPlayer, from)
    else if (controller.hasToBearOff)
    then BearOffMove(from, controller.die)
    else Move(from, controller.die)
  }

  def playerColor = (controller.currentPlayer.toLowerCase)

  def printErr(error: String) =
    printGameWithIndizies(controller.game); println(error)

  def readLine: String =
    printInputFormat; scala.io.StdIn.readLine

  def printInputFormat = {
    if controller.checkersInBar then
      print(s"Dice (${controller.dice.mkString("|")}): ")
    else if (controller.game.winner.isEmpty)
      print(s"${s"${controller.die}".bold} step/s: ")
  }
