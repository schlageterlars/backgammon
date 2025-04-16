package de.htwg.se.backgammon.controller
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import de.htwg.se.backgammon.util.Event
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.base.CustomSetup
import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.core.base.Field
import de.htwg.se.backgammon.core.base.BearInMove

class PutCommandSpec extends AnyWordSpec {
  "PutCommand" should {
    "noStep should do nothing" in {
      val game = new Game(24, 15)
      PutCommand(Move(0, 1)).noStep(game) shouldBe game
    }
    "doStep should do a move" in {
      var game: IGame = new Game(CustomSetup(List(5, 0, 0, -2)))
      game = PutCommand(Move(0, 1)).doStep(game).get
      game.fields should equal(
        List(4, 1, 0, -2, 2, 0, 0, -5).map(i => Field(i))
      )
      game = PutCommand(Move(3, 2)).doStep(game).get
      game = PutCommand(BearInMove(Player.White, steps = 1)).doStep(game).get
      game.fields should equal(
        List(5, -1, 0, -1, 2, 0, 0, -5).map(i => Field(i))
      )
    }
  }
}
