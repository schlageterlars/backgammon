package de.htwg.se.backgammon.core.validate

import scala.util.Try
import scala.util.Success
import scala.util.Failure

trait ValidateStrategy {
  def execute(): Try[Unit] = Try(validate())

  def require(requirement: Boolean, exception: => Exception): Unit =
    if (!requirement) throw exception

  def validate(): Unit
}
