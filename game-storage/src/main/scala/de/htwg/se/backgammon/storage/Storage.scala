package de.htwg.se.backgammon.storage

import java.io.PrintWriter
import scala.util.Using
import java.io.File
import scala.util.Try
import scala.io.Source
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import scala.reflect.ClassTag
import scala.util.boundary
import scala.util.Success
import scala.util.Failure
import de.htwg.se.backgammon.core.storage.Storable

object XmlStorage {
  given Storage = XmlStorage()
}
object JsonStorage {
  given Storage = JsonStorage()
}

trait Storage {
  Parser.initialize()

  def save[O <: Storable: ClassTag](obj: O, path: String): Unit = {
    val clazz = implicitly[ClassTag[O]].runtimeClass.getSimpleName match {
      case clazz: String if clazz.trim().isEmpty() =>
        obj.getClass().getSimpleName()
      case clazz: String => clazz
    }

    Using(PrintWriter(File(s"$path.$fileExtension"))) { pw =>
      pw.write(parse(obj, Parser.get(clazz)))
    }.fold(
      ex => println(s"Error writing to file: ${ex.getMessage}!"),
      _ => boundary
    )
  }

  def load[O <: Storable: ClassTag](path: Option[String]): Try[O] = {
    val className = implicitly[ClassTag[O]].runtimeClass.getSimpleName

    path match {
        case None => Failure(new IllegalArgumentException("Path must be defined"))
        case Some(path) =>
        Parser.get(className) match {
            case Some(parser: Parser[_ <: Storable]) => load(parser, path)
            case None => 
                Failure(new IllegalArgumentException(s"Unsupported type: $className"))
        }
    }
  }

  def load[O <: Storable](parser: Parser[? <: Storable], path: String): Try[O]

  def fileExtension: String

  def parse[O <: Storable](obj: O): String = ???

  def parse[O <: Storable](
      obj: O,
      parser: Option[Parser[? <: Storable]]
  ): String = parse(obj)
}

class XmlStorage extends Storage {
  override def load[O <: Storable](
      parser: Parser[? <: Storable],
      path: String
  ): Try[O] = {
    Try({
      val file = scala.xml.XML.loadFile(s"$path.$fileExtension")
      parser.fromXml(file).asInstanceOf[O]
    })
  }
  override def fileExtension: String = "xml"

  override def parse[O <: Storable](obj: O): String = {
    val xml = new scala.xml.PrettyPrinter(80, 4).format(obj.asXml)
    xml.toString
  }
}

class JsonStorage extends Storage {
  override def load[O <: Storable](
      parser: Parser[? <: Storable],
      path: String
  ): Try[O] = {
    Try({
      val source: String =
        Source.fromFile(s"$path.$fileExtension").getLines.mkString
      val json: JsValue = Json.parse(source)
      parser.fromJson(json).asInstanceOf[O]
    })
  }

  override def fileExtension: String = "json"

  override def parse[O <: Storable](
      obj: O,
      parser: Option[Parser[? <: Storable]]
  ): String = {
    val json = parser.map(p => p.toJson(obj)).getOrElse {
      throw new IllegalArgumentException(
        s"Unsupported json type: ${obj.getClass().getSimpleName()}"
      )
    }; Json.prettyPrint(json)
  }

}
