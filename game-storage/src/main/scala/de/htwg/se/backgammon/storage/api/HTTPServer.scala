package de.htwg.se.backgammon.storage.api

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import de.htwg.se.backgammon.core.IModel
import de.htwg.se.backgammon.core.base.Model
import de.htwg.se.backgammon.storage.api.Routes
import de.htwg.se.backgammon.storage.dao.GameDataDAO
import com.typesafe.config.ConfigFactory
import org.mongodb.scala.MongoClient
import de.htwg.se.backgammon.storage.dao.{MongoGameDataDAO, SlickGameDataDAO}
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.Macros._
import de.htwg.se.backgammon.core.base.database.GameData
import de.htwg.se.backgammon.storage.{GameEntry}
import de.htwg.se.backgammon.core.base.Field
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromCodecs, fromRegistries}
import de.htwg.se.backgammon.storage.dao.codec.{GameDataCodec, GameEntryCodec}
import slick.jdbc.PostgresProfile.api._ 
import slick.jdbc.JdbcBackend.Database
import scala.util.{Success, Failure}


object HttpServer {
  // Move these out of main
  implicit val system: ActorSystem = ActorSystem("Http")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  def main(args: Array[String]): Unit = {
    val db = dao()
    println(s"Initiziled dao. (${db.name})")
    
    val bindingFuture =  Http().bindAndHandle(Routes(db).routes, "0.0.0.0", 8081)
    bindingFuture.failed.foreach { ex =>
      println(s"Failed to bind HTTP endpoint, terminating system. Reason: ${ex.getMessage}")
      system.terminate()
    }

    println("Server started at http://game-storage:8081")
  }


  def dao(): GameDataDAO = {
    val config = ConfigFactory.load()
    val dbType = sys.env.getOrElse("DB_TYPE", "slick")

    dbType.toLowerCase match {
      case "mongo" =>
        val mongoUri = sys.env.getOrElse("MONGO_URI", "mongodb://admin:admin123@mongo:27017")
        val mongoDatabaseName = sys.env.getOrElse("MONGO_DB", "gamedb")

        val mongoClient = MongoClient(mongoUri)
        val database = mongoClient.getDatabase(mongoDatabaseName)
        val codecRegistry = fromRegistries(
          fromCodecs(new GameDataCodec(), new GameEntryCodec()),
          MongoClient.DEFAULT_CODEC_REGISTRY
        )

        val gameDataCollection: MongoCollection[GameData] = database.getCollection("game_data")
        val gameEntryCollection: MongoCollection[GameEntry] = database.getCollection("game_entry")
        val counterCollection: MongoCollection[Document] = database.getCollection("counters")
        counterCollection.insertOne(Document("_id" -> "gameId", "seq" -> 1000)).toFuture()


        database.listCollectionNames().toFuture().onComplete {
        case Success(names) => println(s"MongoDB connected! Collections: ${names.mkString(", ")}")
        case Failure(e) => println(s"MongoDB connection failed: ${e.getMessage}")
        }

        new MongoGameDataDAO(mongoClient, gameDataCollection, gameEntryCollection)

      case "slick" | _ =>
        val db = Database.forConfig("postgres")

        import scala.concurrent.ExecutionContext.Implicits.global

        db.run(sql"SELECT 1".as[Int].head).onComplete {
          case Success(_) => println("Postgres (Slick) connected successfully!")
          case Failure(e) => println(s"Postgres connection failed: ${e.getMessage}")
        }
      
        new SlickGameDataDAO(db)
    }
  }
}