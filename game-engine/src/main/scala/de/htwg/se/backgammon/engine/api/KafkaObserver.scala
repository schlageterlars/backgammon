package de.htwg.se.backgammon.engine.api

import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.core.IGame
import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import de.htwg.se.backgammon.core.Observer
import de.htwg.se.backgammon.core.Event
import de.htwg.se.backgammon.core.IController
import play.api.libs.json.Json
import de.htwg.se.backgammon.core.base.database.GameData
import java.time.Instant

class KafkaObserver(controller: IController) extends Observer {
  controller.add(this)

  implicit val system: ActorSystem = ActorSystem("EngineSystem")
  implicit val mat: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher

  val bootstrapServers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  private val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  private val producer = new KafkaProducer[String, String](props)

  sys.addShutdownHook {
    println("Shutting down KafkaProducer...")
    producer.close()
  }

  override def update(e: Event, exception: Option[Throwable]): Unit = {
    def isKafkaRelevant(event: Event): Boolean = event match {
      case Event.Move | Event.DiceRolled | Event.PlayerChanged => true
      case _ => false
    }

    if (isKafkaRelevant(e)) {
      val json = Json.toJson(GameData(
        id = 0,
        "",
        fields = controller.game.fields,
        barWhite = controller.game.barWhite,
        barBlack = controller.game.barBlack,
        whoseTurn = controller.currentPlayer,
        timestamp = Instant.now.getEpochSecond
      ))
      val record = new ProducerRecord[String, String]("game-events", "game", json.toString)

      producer.send(record, (metadata, exception) => {
        if (exception != null) {
          println(s"❌ Kafka send failed: ${exception.getMessage}")
        } else {
          println(s"✅ Kafka event sent: ${metadata.topic()} [${metadata.partition()}]")
        }
      })
    }
  }
}
