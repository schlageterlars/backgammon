package de.htwg.se.backgammon

import de.htwg.se.backgammon.controller.base.Controller
import de.htwg.se.backgammon.view.TUI
import de.htwg.se.backgammon.view.GUI
import scalafx.application.JFXApp3
import scala.concurrent.Future
import scala.concurrent.Await
import de.htwg.se.backgammon.util.Observer
import de.htwg.se.backgammon.util.Event
import scala.util.Success
import scala.util.Failure
import de.htwg.se.backgammon.model.storage.Storage
import de.htwg.se.backgammon.model.IModel
import de.htwg.se.backgammon.model.base.Model
import de.htwg.se.backgammon.model.base.Game
import de.htwg.se.backgammon.model.base.Dice
import de.htwg.se.backgammon.model.base.DefaultSetup

import de.htwg.se.backgammon.model.storage.XmlStorage.given_Storage


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

    val model: Model = storage.load[IModel]("data") match {
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
