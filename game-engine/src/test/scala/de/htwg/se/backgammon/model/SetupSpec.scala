package de.htwg.se.backgammon.core

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import scala.util.hashing.Hashing.Default
import de.htwg.se.backgammon.core.base.DefaultSetup

class SetupSpec extends AnyWordSpec {
  "Setup" should {
    "not be allowed to define odd number of fields" in {
      an[AssertionError] should be thrownBy DefaultSetup(15, 4)
    }

    "not be allowed to define smaller number than 12 fields" in {
      an[AssertionError] should be thrownBy DefaultSetup(8, 4)
    }

    "calculate default layout as map: '0 -> 2, 5 -> -5, 7 -> -3, 11 -> 5' " in {
      DefaultSetup(24, 15).toMap should equal(
        Map(
          0 -> 2,
          5 -> -5,
          7 -> -3,
          11 -> 5
        )
      )
    }

    "be a map" in {
      DefaultSetup(24, 15).toMap shouldBe a[Map[Int, Int]]
    }

  }
}
