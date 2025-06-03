package de.htwg.se.backgammon.storage.dao.codec

import org.bson._
import org.bson.codecs._
import de.htwg.se.backgammon.storage.{FieldList, GameEntry}

class GameEntryCodec extends Codec[GameEntry] {
  override def encode(writer: BsonWriter, value: GameEntry, encoderContext: EncoderContext): Unit = {
    writer.writeStartDocument()
    writer.writeInt32("id", value.id)
    writer.writeString("name", value.name)
    writer.writeInt32("gameDataId", value.gameDataId)
    writer.writeEndDocument()
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): GameEntry = {
    reader.readStartDocument()
    val id          = reader.readInt32("id")
    val name        = reader.readString("name")
    val gameDataId  = reader.readInt32("gameDataId")
    reader.readEndDocument()

    GameEntry(id, name, gameDataId)
  }

  override def getEncoderClass: Class[GameEntry] = classOf[GameEntry]
}
