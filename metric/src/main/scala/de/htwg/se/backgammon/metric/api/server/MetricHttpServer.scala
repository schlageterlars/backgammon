package metric.api.server

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import metric.api.routes.MetricRoutes
import org.slf4j.LoggerFactory
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import de.htwg.se.backgammon.metric.performance_dao.MongoPerformanceDAO
import org.mongodb.scala.MongoClient
import de.htwg.se.backgammon.metric.METRIC_BASE_URL
import org.mongodb.scala.bson.collection.immutable.Document
import slick.jdbc.JdbcBackend.Database
import de.htwg.se.backgammon.metric.performance_dao.SlickPerformanceDAO

object MetricHttpServer:
  private[server] implicit val system: ActorSystem = ActorSystem(getClass.getSimpleName.init)
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val logger = LoggerFactory.getLogger(getClass.getName.init)

  val mongoClient = MongoClient("mongodb://admin:admin123@localhost:27017")
  val db = Database.forConfig("postgres")

  def run: Future[ServerBinding] = {
    val database = mongoClient.getDatabase("gamedb")

    val ping = database.runCommand(Document("ping" -> 1))

    ping.subscribe(
      (result: Document) => println(s"✅ Connected to MongoDB: $result"),
      (error: Throwable) => println(s"❌ MongoDB connection failed: ${error.getMessage}"),
      () => println("Ping operation complete")
    )

    import scala.concurrent.ExecutionContext.Implicits.global
    val serverBinding = Http()
      .newServerAt("localhost", 8086)
      .bind(routes(MetricRoutes(SlickPerformanceDAO(db))))

/*
    val serverBinding = Http()
      .newServerAt("localhost", 8086)
      .bind(routes(MetricRoutes(MongoPerformanceDAO(mongoClient))))
*/
    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "shutdown-server") { () =>
      shutdown(serverBinding)
    }

    serverBinding.onComplete {
      case Success(binding)   => logger.info(s"Metric Service -- Http Server is running at $METRIC_BASE_URL\n")
      case Failure(exception) => logger.error(s"Metric Service -- Http Server failed to start", exception)
    }
    serverBinding
}

  private def routes(metricRoutes: MetricRoutes): Route =
    pathPrefix("api") {
      concat(
        pathPrefix("metric") {
          concat(
            metricRoutes.metricRoutes
          )
        }
      )
    }

  private def shutdown(serverBinding: Future[ServerBinding]): Future[Done] =
    serverBinding.flatMap { binding =>
      binding.unbind().map { _ =>
        logger.info("Metric Service -- Shutting Down Http Server...")
        system.terminate()
        Done
      }
    }
