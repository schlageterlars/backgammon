package de.htwg.se.backgammon.metric.performance_dao

import org.mongodb.scala.MongoClient
import slick.lifted.TableQuery
import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import de.htwg.se.backgammon.storage.GameDataTable
import slick.jdbc.PostgresProfile.api._
import de.htwg.se.backgammon.core.Player
import de.htwg.se.backgammon.core.base.database.GameData

class SlickPerformanceDAO(db: slick.jdbc.JdbcBackend.Database) extends PerformanceDAO:

    private def moveTable = TableQuery[GameDataTable](new GameDataTable(_))

    db.run(
        DBIO.seq(
        moveTable.schema.dropIfExists,
        moveTable.schema.createIfNotExists
        )
    )

    override def create(timestamp: Long, playerName: String): Try[String] = {
        println(s"Inserting move: $timestamp - $playerName")
        Try {
            val moveID: Int = Await.result(insertMove(timestamp, playerName), 5.seconds)
            println(s"Inserted move with ID: $moveID")
            moveID.toString
        } recover {
            case e: Exception =>
            println("Insert failed: " + e.getMessage)
            throw e // let it bubble up for now
        }
    }


    private def insertMove(timestamp: Long, playerName: String): Future[Int] = {
        val gameData = GameData(
            id = 0,
            name = playerName,
            fields = List.empty,
            barWhite = 0,
            barBlack = 0,
            whoseTurn = Player.White,
            timestamp = timestamp
        )

        val insertAction = (
            moveTable returning moveTable.map(_.id)
        ) += gameData

        db.run(insertAction)
    }
    
    override def getTotalGameDuration: Int =
        val lookupAction = moveTable
        .map(m => m.timestamp)
        .sortBy(_.asc)
        .take(1)
        .join(moveTable
            .map(m => m.timestamp)
            .sortBy(_.desc)
            .take(1))
        .on((a, b) => true)

        val result: Vector[(Long, Long)] = Await.result(db.run(lookupAction.result), 5.seconds).toVector

        result.headOption match
        case Some((firstTimestamp, lastTimestamp)) => Math.round((lastTimestamp - firstTimestamp) / 1000).toInt
        case None => 0

    override def getAvgMoveDuration(playerName: String): Int = {
        val lookupAction = moveTable
            .filter(_.name === playerName)
            .sortBy(_.timestamp)
            .map(_.timestamp)

        val result: Seq[Long] = Await.result(db.run(lookupAction.result), 5.seconds)

        if (result.size > 1) {
            val diffs = result.sliding(2).collect {
            case Seq(prev, next) => next - prev
            }.toSeq

            Math.round(diffs.sum.toDouble / diffs.size / 1000).toInt // in Sekunden
        } else {
            0
        }
    }

    override def getMinMoveDuration(playerName: String): Int = {
        val lookupAction = moveTable
            .filter(_.name === playerName)
            .sortBy(_.timestamp)
            .map(_.timestamp)

        val result: Seq[Long] = Await.result(db.run(lookupAction.result), 5.seconds)

        if (result.size > 1) {
            val diffs = result.sliding(2).collect {
            case Seq(prev, next) => next - prev
            }.toSeq
            Math.round(diffs.min.toDouble / 1000).toInt // in Sekunden
        } else {
            0
        }
    }

    override def getMaxMoveDuration(playerName: String): Int = {
        val lookupAction = moveTable
            .filter(_.name === playerName) // Einheitlich: _.name
            .sortBy(_.timestamp)
            .map(_.timestamp)

        val result: Seq[Long] = Await.result(db.run(lookupAction.result), 5.seconds)

        if (result.size > 1) {
            val diffs = result.sliding(2).collect {
            case Seq(prev, next) => next - prev
            }.toSeq
            Math.round(diffs.max.toDouble / 1000).toInt // in Sekunden
        } else {
            0
        }
    }

    override def getLongestMoveStreak(playerName: String): Int =
        val lookupAction = moveTable
        .sortBy(_.timestamp)
        .map(_.name)

        val result: Vector[String] =
        Await.result(db.run(lookupAction.result), 5.seconds).toVector

        result.foldLeft((0, 0)) { case ((maxStreak, currentStreak), name) =>
        if name == playerName then
            val newStreak = currentStreak + 1
            (math.max(maxStreak, newStreak), newStreak)
        else
            (maxStreak, 0)
        }._1

    override def getNumOfTotalMoves(playerName: String): Int =
        val lookupAction = moveTable
        .filter(_.name === playerName)
        .length
        Await.result(db.run(lookupAction.result), 5.seconds)
