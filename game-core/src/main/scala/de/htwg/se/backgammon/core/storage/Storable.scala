package de.htwg.se.backgammon.model.storage

import scala.xml.Elem

trait Storable() {
  def asXml: Elem = ???
}
