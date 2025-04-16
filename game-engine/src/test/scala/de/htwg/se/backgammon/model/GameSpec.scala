package de.htwg.se.backgammon.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import de.htwg.se.backgammon.exception.{NotYourFieldException, EmptyFieldException, AttackNotPossibleException, FieldDoesNotExistException, DieNotExistException, WrongDirectionException}
import de.htwg.se.backgammon.core.base.Game
import de.htwg.se.backgammon.core.base.Dice
import de.htwg.se.backgammon.core.base.Field
import de.htwg.se.backgammon.core.base.CustomSetup
import de.htwg.se.backgammon.core.base.Move
import de.htwg.se.backgammon.core.base.BearInMove
import de.htwg.se.backgammon.core.base.DefaultSetup

class GameSpec extends AnyWordSpec {
  "Game" should {
    "return value between between 1 and 6" in {
      Dice().roll should be > 0
      Dice().roll should be < 7
    }
    "have default layout '|2| |/| |/| |/| |-5| |/| |-3| |/| |/| |/| |/| |2| " +
      "|-2| |/| |/| |/| |/| |5| |/| |3| |/| |/| |/| |-5|' " in {
        new Game(24, 15).fields should equal(
          List(2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0,
            0, 0, -2).map(i => Field(i))
        )
      }
    "have default layout" in {
        new Game(24, 15).fields should equal(
          List(2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0,
            0, 0, -2).map(i => Field(i))
        )
    }
    "have custom layout" in {
      new Game(CustomSetup(List(4, 0, 0, -2))).fields should equal(
        List(4, 0, 0, -2, 2, 0, 0, -4).map(i => Field(i))
      )
    }
    "return winner empty" in {
      new Game(24, 15).winner shouldBe Option.empty
    }

    "look like |5| |/| |/| |-2| |2| |/| |/| |-5|" in {
      new Game(
        CustomSetup(List(5, 0, 0, -2))
      ).toString shouldBe "0 : |5| |/| |/| |-2| |2| |/| |/| |-5| : 0"
    }
    "attacking opponent, send checker to bar and back" in {
      var game: IGame = new Game(CustomSetup(List(5, 0, 0, -2)))
      game = game.move(Move(0, 1)).get.move(Move(3, 2)).get
      game.fields should equal(
        List(4, -1, 0, -1, 2, 0, 0, -5).map(i => Field(i))
      )
      game.barWhite shouldBe 1

      game = game.move(BearInMove(Player.White, 1)).get
      game.fields should equal(
        List(5, -1, 0, -1, 2, 0, 0, -5).map(i => Field(i))
      )
      game.barWhite shouldBe 0

      game = game.move(Move(0, 1)).get
      game.fields should equal(
        List(4, 1, 0, -1, 2, 0, 0, -5).map(i => Field(i))
      )
      game.barBlack shouldBe 1

      game = game.move(BearInMove(Player.Black, 7)).get
      game.fields should equal(
        List(4, -1, 0, -1, 2, 0, 0, -5).map(i => Field(i))
      )
      game.barWhite shouldBe 1
      game != new Game(16, 24) shouldBe true
      game == game shouldBe true

      game.move(Move(2, 1)).failed.get.getMessage() shouldBe EmptyFieldException(2)
        .getMessage()
      game
        .move(Move(3, 3))
        .failed
        .get
        .getMessage() shouldBe AttackNotPossibleException(3, 0, 4).getMessage()
      game
        .move(Move(3, 15))
        .failed
        .get
        .getMessage() shouldBe FieldDoesNotExistException(3, 9, -12)
        .getMessage()
      DieNotExistException(2, List(1, 3)).getMessage()
      WrongDirectionException(Player.White).getMessage()
      NotYourFieldException(1, Player.White, Player.Black).getMessage()
    }
  }
}
