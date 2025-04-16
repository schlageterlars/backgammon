package de.htwg.se.backgammon.view.component

import javafx.scene.input.MouseEvent
import javafx.event.EventType

trait GUIElement() {
  private var _offsetX: Double = 0
  private var _offsetY: Double = 0

  def isMouseInside(e: MouseEvent): Boolean

  def onHovering(): Unit

  def onMouseExit(): Unit

  final def move(event: MouseEvent): Unit = move(event, _offsetX, _offsetY)

  def move(event: MouseEvent, offsetX: Double, offsetY: Double) = {}

  final def onClicked(
      event: MouseEvent,
      callback: GUIElement => Unit = (e => None)
  ): Unit = {
    callback(this)
    if isDraggable then
      this._offsetX = offsetX(event)
      this._offsetY = offsetY(event)
  }

  final def handleMouseEvent(
      event: MouseEvent,
      doHovering: GUIElement => Boolean = ((e) => true),
      onClicked: GUIElement => Unit = (e => None)
  ): Unit = {
    event.getEventType() match {
      case MouseEvent.MOUSE_MOVED =>
        if isMouseInside(event) && doHovering(this) then onHovering()
        else onMouseExit()
      case MouseEvent.MOUSE_CLICKED if isMouseInside(event) =>
        this.onClicked(event, onClicked)
      case _ =>
    }
    if (isMouseInside(event)) then
      childElements.foreach(c =>
        c.handleMouseEvent(event, doHovering, onClicked)
      )
  }

  def childElements: List[GUIElement] = List()

  def isDraggable: Boolean = false

  def offsetX(event: MouseEvent): Double = 0
  def offsetY(event: MouseEvent): Double = 0
}
