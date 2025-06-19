package metric.gatling.databasePerformanceTest.loadTest

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import scala.concurrent.duration._
import de.htwg.se.backgammon.metric.performance_tests.GatlingSimulationConfig

class DatabaseLoadTest extends GatlingSimulationConfig:
  private val USERS: Int = 20
  private val USER_SPREAD_TIME = USERS.seconds

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
    val scn = buildScenario("Database Load Test Scenario")
    setUp(
      scn.inject(
        rampUsers(USERS) during (USER_SPREAD_TIME)
      )
    ).protocols(httpProtocol)

  executeOperations()
