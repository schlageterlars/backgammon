package de.htwg.se.backgammon.core
import org.scalatest.matchers.should.Matchers._
import de.htwg.se.backgammon.core.base.Field
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.BearInMove
import de.htwg.se.backgammon.core.base.DefinedMove

class MoveSpec extends AnyWordSpec {
  "Move" should {
    "create the right" in {
      val game = new Game(24, 15)
      var move = Move.create(game, Player.White, 0, 2)
      move.steps shouldBe 2

      move = Move.create(game, Player.Black, -1, 22)
      move.steps shouldBe 2
    }
  }
}
