package de.htwg.se.backgammon.storage.api

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl._
import akka.stream.Materializer
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import de.htwg.se.backgammon.core.IGame
import de.htwg.se.backgammon.storage.dao.GameDataDAO
import de.htwg.se.backgammon.core.base.database.GameData
import de.htwg.se.backgammon.core.IModel
import java.time.Instant

object GameEventConsumer {

  def run(db: GameDataDAO)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext): Unit = {
    val bootstrapServers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId("storage-consumer-group")
      .withProperty("auto.offset.reset", "earliest")

    val topic = "game-events"

    Consumer
      .plainSource(consumerSettings, Subscriptions.topics(topic))
      .mapAsync(1) { msg =>
        val jsonStr = msg.value()
        Json.parse(jsonStr).validate[GameData] match {
          case JsSuccess(gameData, _) =>
            println(s"✅ Received game event: ${gameData}")
            db.save(GameData(
              id = 0,
              db.getNickname(), 
              gameData.fields,
              gameData.barWhite,
              gameData.barBlack,
              whoseTurn = gameData.whoseTurn,
              timestamp = gameData.timestamp
            ), nickname = db.getNickname())
            Future.successful(())
          case JsError(errors) =>
            println(s"❌ Invalid IModel JSON: $errors")
            Future.successful(())
        }
      }
      .runWith(Sink.ignore)
  }
}
