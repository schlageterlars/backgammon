package de.htwg.se.backgammon.storage.dao

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext
import de.htwg.se.backgammon.storage.GameEntry
import de.htwg.se.backgammon.storage.dao.GameDataDAO
import de.htwg.se.backgammon.core.base.database.GameData
import scala.util.{Success, Failure}
import de.htwg.se.backgammon.storage.FieldList
import de.htwg.se.backgammon.core.Player
import scala.concurrent.Await
  import scala.concurrent.duration.DurationInt
import com.mongodb.client.model.Updates
import com.mongodb.client.model.FindOneAndUpdateOptions
import org.mongodb.scala.model.ReturnDocument
import org.mongodb.scala.model.Filters
import de.htwg.se.backgammon.core.base.Field
import de.htwg.se.backgammon.storage.api.HttpServer.timeout
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.stream.scaladsl.Sink
import akka.actor.ActorSystem
import akka.stream.Materializer


class MongoGameDataDAO(client: MongoClient, collectionGameData: MongoCollection[GameData], collectionGameEntry: MongoCollection[GameEntry])(implicit ec: ExecutionContext) extends GameDataDAO {
  var nickname: String = ""

  override def getNickname(): String = nickname

  override def setNickname(name: String): Unit =  {
    this.nickname = name
  }

  private implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  private implicit val materializer: Materializer = Materializer(system)

  initCounterIfNotExists()

  override def save(data: GameData, nickname: String): Future[Int] = {
    val source = Source.single(data)

    val processingFlow: Flow[GameData, Int, NotUsed] =
      Flow[GameData].mapAsync(1) { gameData =>
        findGameDataByNickname(gameData.name).flatMap {
          case Some(existingGame) =>
            println(s"✅ Found game data for name ${gameData.name} with id ${existingGame.id}")
            updateGameData(existingGame.id, gameData).map { modifiedCount =>
              println(s"🔁 UpdateGameData finished with modified count: $modifiedCount")
              modifiedCount
            }.recover {
              case ex =>
                println(s"❌ UpdateGameData outer failure: ${ex.getMessage}")
                0
            }

          case None =>
            println(s"🆕 No existing game data found for name ${gameData.name}, inserting new game")
            insert(gameData, nickname)
        }
      }.recover {
        case ex =>
          println(s"❌ Find failed with error: ${ex.getMessage}, do nothing")
          0
      }

    source.via(processingFlow).runWith(Sink.head)
  }


  private val collection: MongoCollection[Document] = client.getDatabase("gamedb").getCollection("game_data")
  private val collectionEntries: MongoCollection[Document] = client.getDatabase("gamedb").getCollection("game_entry")

  // Convert GameData to MongoDB Document with _id
  private def toDocument(gameData: GameData): Document = {
    Document(
      "_id" -> gameData.id,
      "name" -> gameData.name,
      "fields" -> gameData.fields.map(f => Document("pieces" -> f.pieces)), 
      "barWhite" -> gameData.barWhite,
      "barBlack" -> gameData.barBlack,
      "whoseTurn" -> gameData.whoseTurn.toString()
    )
  }

  // Convert MongoDB Document to GameData
  def fromDocument(doc: Document): GameData = {
    import scala.jdk.CollectionConverters._
    val fieldsArray = doc.get("fields").map {
      case arr: org.mongodb.scala.bson.BsonArray =>
        arr.getValues.asScala.toList.collect {
          case d: org.mongodb.scala.bson.BsonDocument =>
            Field(Document(d).getInteger("pieces"))
        }
      case _ => Nil
    }.getOrElse(Nil)

    GameData(
      id = doc.getInteger("_id"),
      name = doc.getString("name"),
      fields = fieldsArray,
      barWhite = doc.getInteger("barWhite"),
      barBlack = doc.getInteger("barBlack"),
      whoseTurn = Player.withName(doc.getString("whoseTurn")),
      timestamp = doc.getLong("timestamp")
    )
  }

  // Insert 
  override def insert(gameData: GameData, nickname: String): Future[Int] = {
    getNextGameId().flatMap { newId =>
      val dataWithId = gameData.copy(id = newId)
      val future = collection.insertOne(toDocument(dataWithId)).toFuture()

      future.onComplete {
        case scala.util.Success(_) =>
          println(s"✅ Insert successful for game with ID: $newId by $nickname")
        case scala.util.Failure(ex) =>
          println(s"❌ Insert failed for ID: $newId. Error: ${ex.getMessage}")
      }

      future.map(_ => newId)
    }
  }


  // Update by _id
  def updateGameData(id: Int, updatedGameData: GameData): Future[Int] = {
      import org.mongodb.scala.model.Updates._
      import scala.util.{Success, Failure}
      import scala.concurrent.ExecutionContext.Implicits.global

      println(s"Update game data in mongo DB with id $id")
      println(updatedGameData)

      val gameData = updatedGameData.copy(id = id)

      val updateDefinition = combine(
        set("fields", updatedGameData.fields),
        set("barWhite", updatedGameData.barWhite),
        set("barBlack", updatedGameData.barBlack),
        set("whoseTurn", updatedGameData.whoseTurn)
      )

        // Return the modified count
       collection.replaceOne(equal("_id", id), toDocument(gameData)).toFuture().map(_.getModifiedCount.toInt).recover { case _ => 0 }
  }


  // Get by id
  def getGameData(id: Int): Future[Option[GameData]] = {
    collection.find(equal("_id", id)).first().toFutureOption().map(_.map(fromDocument))
  }

  // Delete by id
  def deleteGameData(id: Int): Unit = {
    collection.deleteOne(equal("_id", id)).toFuture()
  }

  def getNextGameId(): Future[Int] = {
    val counterCollection: MongoCollection[Document] = client.getDatabase("gamedb").getCollection("counters")
    counterCollection.findOneAndUpdate(
      Filters.equal("_id", "gameId"),
      Updates.inc("seq", 1),
      FindOneAndUpdateOptions()
        .upsert(true)
        .returnDocument(ReturnDocument.AFTER)
    ).toFuture().map(doc => doc.getInteger("seq"))
  }

  override def findGameDataByNickname(nickname: String): Future[Option[GameData]] = {
    /*collectionGameData.find(equal("name", nickname)).first().toFutureOption().map {
      case Some(game) => println(s"Found game: ${game.id}")
      case None       => println("No game found.")
    }*/
    collection.find(equal("name", nickname)).first().toFutureOption().map(_.map(fromDocument))
  }

  override def name: String = "mongo"

  def initCounterIfNotExists(): Future[Unit] = {
    val filter = equal("_id", "gameId")
    
    val counterCollection: MongoCollection[Document] = client.getDatabase("gamedb").getCollection("counters")
    counterCollection.find(filter).first().toFuture().flatMap { docOpt =>
      if (docOpt == null) {
        // Document doesn't exist, insert it
        counterCollection.insertOne(Document("_id" -> "gameId", "seq" -> 1000)).toFuture().map(_ => ())
      } else {
        // Document exists, do nothing
        Future.successful(())
      }
    }
  }
}
