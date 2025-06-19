package metric.gatling.databasePerformanceTest.spikeTest

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import scala.concurrent.duration._
import de.htwg.se.backgammon.metric.performance_tests.GatlingSimulationConfig


class DatabaseSpikeTest extends GatlingSimulationConfig:
  private val SPIKE_USERS = 200
  private val RAMP_UP_USERS = 10
  private val COOL_DOWN_USERS = 5

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
    val scn = buildScenario("Database Spike Test Scenario")
    setUp(
      scn.inject(
        rampUsers(RAMP_UP_USERS) during (10.seconds),
        nothingFor(5.seconds),
        atOnceUsers(SPIKE_USERS),
        nothingFor(10.seconds),
        rampUsers(COOL_DOWN_USERS) during (10.seconds)
      )
    ).protocols(httpProtocol)

  executeOperations()
