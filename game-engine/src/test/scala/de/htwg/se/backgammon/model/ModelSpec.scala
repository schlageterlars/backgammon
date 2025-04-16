package de.htwg.se.backgammon.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.Model

class ModelSpec extends AnyWordSpec {
  "Model" should {
    "that it's the next player's turn" in {
      var t = model
      t.next shouldBe Player.Black
      t.next shouldBe Player.White
    }
    "that it always stores the previous game" in {
      val t = model;
      val game2 = new Game(24, 15)
      t.game = game2
      val game3 = new Game(24, 15)
      t.game = game3
      t.previousGame shouldBe game2
    }
    "White player starts by default" in {
      val game = new Game(24, 15)
      val m = model
      m.player shouldBe Player.White
      m.player.toLowerCase shouldBe "white"
      m.game shouldBe game
    }
  }
  private def model = Model(new Game(24, 15), Player.White, DiceStub(1, 2))
}
