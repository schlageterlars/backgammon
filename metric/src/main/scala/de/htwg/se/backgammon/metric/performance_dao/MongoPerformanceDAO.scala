
package de.htwg.se.backgammon.metric.performance_dao

import java.util.UUID
import org.bson.Document
import org.mongodb.scala.MongoCollection
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

class MongoPerformanceDAO(client: MongoClient) extends PerformanceDAO:

  private val gameDataCollection: MongoCollection[Document] = client.getDatabase("gamedb").getCollection("game_data")

  override def create(timestamp: Long, playerName: String): Try[String] = Try {
    val uuid = UUID.randomUUID().toString
    val doc = new Document()
      .append("timestamp", timestamp)
      .append("name", playerName)
    Await.result(gameDataCollection.insertOne(doc).toFuture, 5.seconds)
    uuid
  }

  override def getTotalGameDuration: Int = {
    val sortedTimestamps = Await.result(
      gameDataCollection.find()
        .sort(ascending("timestamp"))
        .projection(fields(include("timestamp"), excludeId()))
        .map(_.getLong("timestamp"))
        .toFuture(),
      5.seconds
    )
    if sortedTimestamps.size >= 2 then
      val duration = sortedTimestamps.last - sortedTimestamps.head
      (duration / 1000).toInt
    else 0
  }

  override def getAvgMoveDuration(playerName: String): Int = {
    val timestamps = Await.result(
      gameDataCollection.find(equal("name", playerName))
        .sort(ascending("timestamp"))
        .projection(fields(include("timestamp"), excludeId()))
        .map(_.getLong("timestamp"))
        .toFuture(),
      5.seconds
    )
    if timestamps.size > 1 then
      val diffs = timestamps.sliding(2).collect { case Seq(a, b) => b - a }.toSeq
      (diffs.sum.toDouble / diffs.size / 1000).round.toInt
    else 0
  }

  override def getMinMoveDuration(playerName: String): Int = {
    val timestamps = Await.result(
      gameDataCollection.find(equal("name", playerName))
        .sort(ascending("timestamp"))
        .projection(fields(include("timestamp"), excludeId()))
        .map(_.getLong("timestamp"))
        .toFuture(),
      5.seconds
    )
    if timestamps.size > 1 then
      val diffs = timestamps.sliding(2).collect { case Seq(a, b) => b - a }.toSeq
      (diffs.min.toDouble / 1000).round.toInt
    else 0
  }

  override def getMaxMoveDuration(playerName: String): Int = {
    val timestamps = Await.result(
      gameDataCollection.find(equal("name", playerName))
        .sort(ascending("timestamp"))
        .projection(fields(include("timestamp"), excludeId()))
        .map(_.getLong("timestamp"))
        .toFuture(),
      5.seconds
    )
    if timestamps.size > 1 then
      val diffs = timestamps.sliding(2).collect { case Seq(a, b) => b - a }.toSeq
      (diffs.max.toDouble / 1000).round.toInt
    else 0
  }

  override def getLongestMoveStreak(playerName: String): Int = {
    val players = Await.result(
      gameDataCollection.find()
        .sort(ascending("timestamp"))
        .projection(fields(include("name"), excludeId()))
        .map(_.getString("name"))
        .toFuture(),
      5.seconds
    )

    players.foldLeft((0, 0)) { case ((maxStreak, currentStreak), name) =>
      if name == playerName then
        val newStreak = currentStreak + 1
        (math.max(maxStreak, newStreak), newStreak)
      else
        (maxStreak, 0)
    }._1
  }

  override def getNumOfTotalMoves(playerName: String): Int = {
    Await.result(
      gameDataCollection.countDocuments(equal("name", playerName)).toFuture(),
      5.seconds
    ).toInt
  }
