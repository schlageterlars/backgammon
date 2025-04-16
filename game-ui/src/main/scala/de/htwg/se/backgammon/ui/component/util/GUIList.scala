package de.htwg.se.backgammon.view.component

import scalafx.scene.Group
import scala.collection.mutable.ListBuffer
import scalafx.scene.Node

class GUIList[E <: Node] extends Group, IndexedSeq[E] {
  private var elements: ListBuffer[E] = ListBuffer()

  def this(list: List[E]) = {
    this(); set(list)
  }

  def set(list: List[E]) = {
    clear(); list.foreach(e => add(e))
  }

  def add(element: E) = {
    elements += element
    children.add(element)
  }

  def clear() = {
    elements = ListBuffer()
    children = Seq()
  }

  def asList: List[E] = elements.toList

  override def apply(i: Int): E = elements(i)

  override def length: Int = elements.length

}
