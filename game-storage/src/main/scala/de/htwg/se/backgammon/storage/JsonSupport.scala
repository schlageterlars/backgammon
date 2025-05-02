package de.htwg.se.backgammon.storage

import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.util.ByteString
import play.api.libs.json._
import akka.http.scaladsl.unmarshalling.Unmarshaller.messageUnmarshallerFromEntityUnmarshaller

object PlayJsonSupport {

  // Base unmarshaller for HttpEntity to A
  implicit def playJsonUnmarshallerEntity[A](implicit reads: Reads[A]): Unmarshaller[HttpEntity, A] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(ContentTypes.`application/json`)
      .map { jsonString =>
        Json.parse(jsonString).as[A]
      }

  // This lifts it into FromRequestUnmarshaller (what Akka expects in entity(as[X]))
  implicit def playJsonUnmarshaller[A](implicit reads: Reads[A]): FromRequestUnmarshaller[A] =
    messageUnmarshallerFromEntityUnmarshaller(playJsonUnmarshallerEntity[A])

  // Marshaller for writing JSON responses
  implicit def playJsonMarshaller[A](implicit writes: Writes[A]): ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`) { a =>
      HttpEntity(ContentTypes.`application/json`, Json.stringify(Json.toJson(a)))
    }
}
