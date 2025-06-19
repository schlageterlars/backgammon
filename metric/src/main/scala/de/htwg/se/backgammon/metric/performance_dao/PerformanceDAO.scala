package de.htwg.se.backgammon.metric.performance_dao

trait PerformanceDAO {

  /** Inserts a new move with timestamp and player name */
  def create(timestamp: Long, playerName: String): scala.util.Try[String]

  /** Total game duration (in seconds) from first to last timestamp */
  def getTotalGameDuration: Int

  /** Average duration (in seconds) between moves of a specific player */
  def getAvgMoveDuration(playerName: String): Int

  /** Minimum time (in seconds) between two consecutive moves of a player */
  def getMinMoveDuration(playerName: String): Int

  /** Maximum time (in seconds) between two consecutive moves of a player */
  def getMaxMoveDuration(playerName: String): Int

  /** Longest streak of consecutive moves made by the same player */
  def getLongestMoveStreak(playerName: String): Int

  /** Total number of moves made by a specific player */
  def getNumOfTotalMoves(playerName: String): Int
}

