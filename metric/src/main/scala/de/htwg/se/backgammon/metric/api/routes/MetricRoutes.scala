package metric.api.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsValue, Json, OFormat}
import scala.util.{Failure, Success}
import de.htwg.se.backgammon.metric.api.{PlayerStats, GameStats}
import de.htwg.se.backgammon.metric.performance_dao.PerformanceDAO
import de.htwg.se.backgammon.storage.PlayJsonSupport._
import de.htwg.se.backgammon.core.api.PlayJsonSupport._
import de.htwg.se.backgammon.metric.api.GetStatsRequest
import de.htwg.se.backgammon.metric.api.de.htwg.se.backgammon.metric.api.InsertGameRequest

class MetricRoutes(dao: PerformanceDAO) {

  private val logger = LoggerFactory.getLogger(getClass.getName.init)

  def metricRoutes: Route = handleExceptions(exceptionHandler) {
    concat(
      handlePreConnectRequest,
      handleInsertNewMoveRequest,
      handleGetStatsRequest
    )
  }

  private def handlePreConnectRequest: Route = get {
    path("preConnect") {
      complete(StatusCodes.OK)
    }
  }

  private def handleInsertNewMoveRequest: Route = post {
    path("insertMove") {
      entity(as[InsertGameRequest]) { request => {
        println(s"Request received: $request")
        dao.create(request.timestamp, request.playerName) match
          case Success(moveID) =>
            logger.info(s"Metric Service -- Move successfully inserted into database [ID: $moveID]")
            complete(StatusCodes.OK)
          case Failure(exception) =>
            logger.error(s"Metric Service -- Failed to insert Move into database: ${exception.getMessage}")
            complete(StatusCodes.InternalServerError)
        } 
      }
    }
    
  }


  private def handleGetStatsRequest: Route = post {
    path("getStats") {
      entity(as[GetStatsRequest]) { request =>
        val playerStats: Map[String, PlayerStats] = request.playerNames.map { name =>
          name -> PlayerStats(
            avgMoveDuration     = dao.getAvgMoveDuration(name),
            minMoveDuration     = dao.getMinMoveDuration(name),
            maxMoveDuration     = dao.getMaxMoveDuration(name),
            longestMoveStreak   = dao.getLongestMoveStreak(name),
            numOfTotalMoves     = dao.getNumOfTotalMoves(name)
          )
        }.toMap

        val result = GameStats(
          totalDuration = dao.getTotalGameDuration,
          playerStats   = playerStats
        )

        complete(result)
      }
    }
  }

  private val exceptionHandler = ExceptionHandler {
    case e: IllegalArgumentException =>
      complete(Conflict -> e.getMessage)
    case e: Throwable =>
      complete(InternalServerError -> e.getMessage)
  }
}