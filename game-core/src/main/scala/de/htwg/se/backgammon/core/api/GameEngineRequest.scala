package de.htwg.se.backgammon.core.api

package core.api.service

import play.api.libs.json.Json
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import de.htwg.se.backgammon.core.api.ApiClient
import com.typesafe.config.ConfigFactory
import de.htwg.se.backgammon.core.base.Move
import play.api.libs.json._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import de.htwg.se.backgammon.core.api.PlayJsonSupport._


implicit def playJsonMarshaller[T](implicit writes: Writes[T]): ToEntityMarshaller[T] =
  Marshaller.withFixedContentType(ContentTypes.`application/json`) { obj =>
    val js = Json.toJson(obj)
    HttpEntity(ContentTypes.`application/json`, Json.stringify(js))
  }

object GameEngineRequest {
  private val client = ApiClient(ConfigFactory.load().getString("api.url"))

  def put(move: Move): Either[Option[Unit], Throwable] = {
    val result = Await.result(client.postRequest[Move]("api/put/move", move), 10.seconds)
    result match {
        case Right(_)     => Left(None)  
        case Left(error)  => Right(error) 
    }
  }
}