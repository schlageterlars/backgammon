package de.htwg.se.backgammon.util

import scala.util.Try
import scala.util.Success

trait Command[T, M]:
  def noStep(t: T): T
  def doStep(t: T): Try[T]
  def undoStep(): M

class Manager[T, M]:
  private var undoStack: List[Command[T, M]] = Nil
  private var redoStack: List[Command[T, M]] = Nil

  def doStep(t: T, command: Command[T, M]): Try[T] =
    undoStack = command :: undoStack
    command.doStep(t)

  def undoStep(): Option[M] =
    undoStack match {
      case Nil => None
      case head :: tail => {
        val result = head.undoStep()
        undoStack = tail
        redoStack = head :: redoStack
        Some(result)
      }

    }

  def stackCommand = undoStack.headOption

  def redoStep(t: T): Try[T] =
    redoStack match {
      case Nil => Success(t)
      case head :: tail => {
        val result = head.doStep(t)
        redoStack = tail
        undoStack = head :: undoStack
        result
      }
    }
