package de.htwg.se.backgammon.view

import scala.util.Try
import de.htwg.se.backgammon.controller.IController
import de.htwg.se.backgammon.exception.MoveException
import de.htwg.se.backgammon.model.base.Move
import de.htwg.se.backgammon.model.base.BearInMove
import de.htwg.se.backgammon.util.Event
import de.htwg.se.backgammon.util.Observer
import de.htwg.se.backgammon.util.PrettyPrint.MarkDifferencesBetweenGames
import de.htwg.se.backgammon.util.PrettyPrint.PrintDiceResults
import de.htwg.se.backgammon.util.PrettyPrint.PrintBold
import de.htwg.se.backgammon.util.PrettyPrint.printNew
import de.htwg.se.backgammon.util.PrettyPrint.printGameWithIndizies
import de.htwg.se.backgammon.model.Input
import de.htwg.se.backgammon.model.Quit
import de.htwg.se.backgammon.model.Undo
import de.htwg.se.backgammon.model.Redo
import de.htwg.se.backgammon.model.base.BearOffMove
import de.htwg.se.backgammon.model.Skip
import de.htwg.se.backgammon.util.PrettyPrint.printGameOver

class TUI(controller: IController, out: String => Unit = (msg: String) => println(msg)) extends Observer:
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
        out(s"${s"${controller.currentPlayer}".bold} it's your turn!")
      case Event.DiceRolled =>
        out(
          s"You rolled the dice twice: ${controller.dice.toPrettyString}"
        )
      case Event.BarIsNotEmpty =>
        out(
          "It is at least one checker in your bar, which die do you wanna use?"
        )
      case Event.AllCheckersInTheHomeBoard =>
        out(
          "All of your checkers are on your homeboard, so you can bear off."
        )
      case Event.GameOver => printGameOver(controller.game.winner.get)

  def inputLoop(): Unit =
    execute(analyseInput(readLine))
    
    if continue then inputLoop()

  def execute(input: Option[Input]): Unit = 
    input match 
      case None if controller.checkersInBar =>
        printErr(s"Use: {steps from ${playerColor.bold} bar}")
      case None =>
        printErr(s"Use: {field with ${playerColor.bold} checkers}")
      case Some(move: Move) => controller.doAndPublish(controller.put, move)
      case Some(_: Redo)    => controller.doAndPublish(controller.redo)
      case Some(_: Undo)    => controller.undoAndPublish(controller.undo)
      case Some(_: Skip)    => controller.skip(controller.die)
      case Some(_)          => controller.quit

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
    printGameWithIndizies(controller.game); out(error)

  def readLine: String =
    printInputFormat; scala.io.StdIn.readLine

  def printInputFormat = {
    if controller.checkersInBar then
      print(s"Dice (${controller.dice.mkString("|")}): ")
    else if (controller.game.winner.isEmpty)
      print(s"${s"${controller.die}".bold} step/s: ")
  }
