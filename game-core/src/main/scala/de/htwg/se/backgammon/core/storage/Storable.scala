package de.htwg.se.backgammon.core.storage

import scala.xml.Elem

trait Storable() {
  def asXml: Elem = ???
}
