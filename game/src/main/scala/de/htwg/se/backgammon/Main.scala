package de.htwg.se.backgammon

import de.htwg.se.backgammon.engine.controller.base.Controller
import de.htwg.se.backgammon.ui.GUI
import de.htwg.se.backgammon.ui.TUI
import scalafx.application.JFXApp3
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure
import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.core.base.Model
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.Dice
import de.htwg.se.backgammon.core.base.DefaultSetup

import de.htwg.se.backgammon.storage.Storage

import de.htwg.se.backgammon.storage.JsonStorage.given_Storage
import de.htwg.se.backgammon.core.Event
import de.htwg.se.backgammon.core.Observer


private val NUMBER_OF_FIELDS = 24
private val NUMBER_OF_FIGURES = 15

object Main {
  val controller: Controller = configurate()
  val tui: TUI = new TUI(controller)
  val gui: GUI = new GUI(controller)

  @main def run(): Unit = {
    implicit val context = scala.concurrent.ExecutionContext.global
    val f = Future {
      gui.main(Array[String]())
    }
    tui.run
    Await.ready(f, scala.concurrent.duration.Duration.Inf)
  }

  def configurate()(using storage: Storage): Controller = {
    def changed(e: Event): Boolean = e match {
      case Event.Move | Event.DiceRolled | Event.PlayerChanged => true
      case _                                                   => false
    }

    val model: Model = storage.load[IModel](Some("data")) match {
      case Success(obj: Model) => obj
      case _ =>
        new Model(
          new Game(DefaultSetup(NUMBER_OF_FIELDS, NUMBER_OF_FIGURES)),
          new Dice()
        )
    }
    val controller = Controller(model)
    controller.add(new Observer {
      override def update(e: Event, exception: Option[Throwable]): Unit =
        if changed(e) then storage.save(controller.data, "data")
    }); controller
  }
}
