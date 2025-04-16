package de.htwg.se.backgammon.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.Player
import base.Controller
import de.htwg.se.backgammon.core.base.Model
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.CustomSetup
import de.htwg.se.backgammon.core.DiceStub
import de.htwg.se.backgammon.core.base.BearInMove
import de.htwg.se.backgammon.core.base.BearOffMove
import de.htwg.se.backgammon.util.Observer
import de.htwg.se.backgammon.util.Event

class ControllerSpec extends AnyWordSpec {
  "Controller" should {
    "that it init right" in {
      val c = controller
      c.dice.length shouldBe 2
      val previousGame = c.game
      c.doAndPublish(c.put, Move(0, 1))
      c.previousGame shouldBe previousGame
      c.die shouldBe 2

      c.doAndPublish(c.put, Move(0, 5))
      c.doAndPublish(c.put, Move(0, 2))
      c.currentPlayer shouldBe Player.Black

    }

    "observer notify on move" in {
      val controller = Controller(m)
      var notified = false
      controller.add(new Observer {
        override def update(e: Event, exception: Option[Throwable]): Unit =
          notified = true
      })
      controller.doAndPublish(controller.put, Move(0, 1))
      notified shouldBe true
    }

    "a whole game should work " in {
      val controller = Controller(m)

      controller.doAndPublish(controller.redo) // nothing to redo
      controller.game.fields shouldBe List(2, 0, 0, -1, 1, 0, 0, -2)

      // White moves (1,2)
      controller.doAndPublish(controller.put, Move(0, 1))
      controller.game.fields shouldBe List(1, 1, 0, -1, 1, 0, 0, -2)

      controller.doAndPublish(controller.put, Move(0, 5)) // nicht gültig
      controller.doAndPublish(controller.put, Move(0, 2))
      controller.game.fields shouldBe List(0, 1, 1, -1, 1, 0, 0, -2)

      // Black moves(5,5)
      controller.doAndPublish(controller.put, Move(7, 5))
      controller.doAndPublish(controller.put, Move(7, 5))
      controller.doAndPublish(controller.redo)
      controller.game.fields shouldBe List(0, 1, -2, -1, 1, 0, 0, 0)
      controller.game.barWhite shouldBe 1

      // Black moves(3,2) doublets
      controller.doAndPublish(controller.put, Move(3, 3))
      controller.game.fields shouldBe List(-1, 1, -2, 0, 1, 0, 0, 0)
      controller.doAndPublish(controller.put, Move(2, 2))
      controller.game.fields shouldBe List(-2, 1, -1, 0, 1, 0, 0, 0)

      // White moves(3,2)
      controller.doAndPublish(controller.put, BearInMove(Player.White, 2))
      controller.game.fields shouldBe List(-2, 2, -1, 0, 1, 0, 0, 0)
      controller.game.barWhite shouldBe 0
      controller.doAndPublish(controller.put, Move(1, 3))
      controller.game.fields shouldBe List(-2, 1, -1, 0, 2, 0, 0, 0)

      // Black moves (1,2)
      controller.dice shouldBe List(1, 2)
      controller.doAndPublish(controller.put, BearOffMove(0, 1))
      controller.game.fields shouldBe List(-1, 1, -1, 0, 2, 0, 0, 0)
      controller.doAndPublish(controller.put, Move(2, 2))
      controller.game.fields shouldBe List(-2, 1, 0, 0, 2, 0, 0, 0)

      // White moves(4,3)
      controller.dice shouldBe List(4, 3)
      controller.doAndPublish(controller.put, Move(1, 4))
      controller.game.fields shouldBe List(-2, 0, 0, 0, 2, 1, 0, 0)

      // undo
      controller.undoAndPublish(controller.undo)
      controller.game.fields shouldBe List(-2, 1, 0, 0, 2, 0, 0, 0)

      // do same again
      controller.doAndPublish(controller.put, Move(1, 4))
      controller.game.fields shouldBe List(-2, 0, 0, 0, 2, 1, 0, 0)

      // ** do step and undo
      controller.dice shouldBe List(3)
      controller.doAndPublish(controller.put, Move(4, 3))
      controller.game.fields shouldBe List(-2, 0, 0, 0, 1, 1, 0, 1)
      // **

      // Black moves(1,1)
      controller.currentPlayer shouldBe Player.Black
      controller.doAndPublish(controller.put, BearOffMove(0, 1))
      controller.game.fields shouldBe List(-1, 0, 0, 0, 1, 1, 0, 1)

      controller.doAndPublish(controller.put, BearOffMove(0, 1))
      controller.game.fields shouldBe List(0, 0, 0, 0, 1, 1, 0, 1)

      // ** Game finished - Black won **//
      controller.game.winner.get shouldBe Player.Black
    }
  }

  private def m = {
    Model(
      new Game(CustomSetup(List(2, 0, 0, -1))),
      Player.White,
      DiceStub(1, 2, 5, 5, 3, 2, 3, 2, 1, 2, 4, 3, 1, 1, 3, 5)
    )
  }

  private def controller = Controller(m)

}
