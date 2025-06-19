package metric.gatling.databasePerformanceTest.stressTest

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import de.htwg.se.backgammon.metric.performance_tests.GatlingSimulationConfig
import scala.concurrent.duration._

class DatabaseStressTest extends GatlingSimulationConfig:
  private val USERS: Int = 3000
  private val USER_SPREAD_TIME = 20.seconds

  override val operations: List[ChainBuilder] = List(
    buildOperation(
      "save move",
      "POST",
      "/api/metric/insertMove",
      StringBody(moveAsJsonString)
    ),
    buildOperation(
      "get stats",
      "POST",
      "/api/metric/getStats",
      StringBody(getStatsToJsonString)
    )
  )

  override def executeOperations(): Unit =
    val scn = buildScenario("Database Stress Test Scenario")
    setUp(
      scn.inject(
        stressPeakUsers(USERS) during (USER_SPREAD_TIME)
      )
    ).protocols(httpProtocol)

  executeOperations()
