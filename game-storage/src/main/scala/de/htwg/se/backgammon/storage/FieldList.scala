package de.htwg.se.backgammon.storage

import de.htwg.se.backgammon.core.IField
import de.htwg.se.backgammon.core.base.Field

final class FieldList (val fields: List[IField]) {
  def to(): String = {
    fields.map(_.pieces).mkString(",")
  }
}

object FieldList {
  def from(string: String): FieldList = {
    val fields = string
      .split(",")
      .map(s => Field(s.trim.toInt))
      .toList
    FieldList(fields)
  }
}
