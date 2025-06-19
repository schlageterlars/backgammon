package de.htwg.se.backgammon.storage.dao.codec

import org.bson._
import org.bson.codecs._
import de.htwg.se.backgammon.core.base.database.GameData
import de.htwg.se.backgammon.storage.FieldList

class GameDataCodec extends Codec[GameData] {
  override def encode(writer: BsonWriter, value: GameData, encoderContext: EncoderContext): Unit = {
    writer.writeStartDocument()
    writer.writeInt32("id", value.id)
    writer.writeString("name", value.name)
    writer.writeString("fields", FieldList(value.fields).to())
    writer.writeInt32("barWhite", value.barWhite)
    writer.writeInt32("barBlack", value.barBlack)
    writer.writeString("whoseTurn", value.whoseTurn.toString)
    writer.writeEndDocument()
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): GameData = {
    reader.readStartDocument()
    val id        = reader.readInt32("id")
    val name = reader.readString("name")
    val fieldsStr = reader.readString("fields")
    val fields    = FieldList.from(fieldsStr).fields
    val barWhite  = reader.readInt32("barWhite")
    val barBlack  = reader.readInt32("barBlack")
    val turn      = reader.readString("whoseTurn")
    val timestamp = reader.readDouble("timestamp")

    reader.readEndDocument()

    GameData(id, name, fields, barWhite, barBlack, de.htwg.se.backgammon.core.Player.withName(turn), timestamp.toLong)
  }

  override def getEncoderClass: Class[GameData] = classOf[GameData]
}
