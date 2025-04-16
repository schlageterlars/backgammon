package de.htwg.se.backgammon.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import de.htwg.se.backgammon.core.base.Field

class FieldSpec extends AnyWordSpec {
  "Field" should {
    "be equal to same Field" in {
      Field(5) should equal(Field(5))
    }
    "be equal to same number of pieces" in {
      Field(5) should equal(5)
    }
    "be different to a different Field" in {
      Field(5) should not equal (Field(10))
    }
    "be different to a different type of object" in {
      Field(5) should not equal ("test")
    }
    "be 0 when calling the empty constructor" in {
      Field() should equal(0)
    }
    "look like |5| when number of pieces is 5" in {
      Field(5).toString shouldBe "|5|"
    }
    "look like |/| when number of pieces is 0" in {
      Field().toString shouldBe "|/|"
    }
    "with player white" in {
      Field(Player.White).pieces shouldBe 1
    }
    "with player black" in {
      Field(Player.Black).pieces shouldBe -1
    }
    "with player none" in {
      Field(Player.None).pieces shouldBe 0
      Field(Player.None).isOccupied() shouldBe false
    }
  }
}
