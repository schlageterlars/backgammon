package de.htwg.se.backgammon.core.api

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.model._
import akka.util.ByteString
import play.api.libs.json._

import scala.util.control.NonFatal

object PlayJsonSupport {

  // Marshaller: Scala object → JSON entity
  implicit def playJsonMarshaller[T](implicit writes: Writes[T]): ToEntityMarshaller[T] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`) { obj =>
      val json = Json.toJson(obj)
      HttpEntity(ContentTypes.`application/json`, Json.stringify(json))
    }

  // Unmarshaller: JSON entity → Scala object
  implicit def playJsonUnmarshaller[T](implicit reads: Reads[T]): FromEntityUnmarshaller[T] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(ContentTypes.`application/json`)
      .mapWithCharset { (data: ByteString, charset) =>
        val jsonString = data.decodeString(charset.nioCharset.name())
        val json = Json.parse(jsonString)
        json.validate[T] match {
          case JsSuccess(value, _) => value
          case JsError(errors) =>
            throw new RuntimeException(s"JSON deserialization error: ${JsError.toJson(errors)}")
        }
      }
}
