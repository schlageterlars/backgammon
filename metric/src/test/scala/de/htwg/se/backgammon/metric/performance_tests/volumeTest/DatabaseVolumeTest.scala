package metric.gatling.databasePerformanceTest.volumeTest

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import scala.concurrent.duration._
import de.htwg.se.backgammon.metric.performance_tests.GatlingSimulationConfig

class DatabaseVolumeTest extends GatlingSimulationConfig:
  private val USERS: Int = 10
  private val REPEAT_COUNT = 1

  override val operations: List[ChainBuilder] = List(
    repeat(REPEAT_COUNT) {
      buildOperation(
        "save move",
        "POST",
        "/api/metric/insertMove",
        StringBody(moveAsJsonString)
      )
    },
    repeat(REPEAT_COUNT) {
      buildOperation(
        "get stats",
        "POST",
        "/api/metric/getStats",
        StringBody(getStatsToJsonString)
      )
    }
  )

  override def executeOperations(): Unit =
    val scn = buildScenario("Database Volume Test Scenario")
    setUp(
      scn.inject(atOnceUsers(USERS))
    ).protocols(httpProtocol)

  executeOperations()
