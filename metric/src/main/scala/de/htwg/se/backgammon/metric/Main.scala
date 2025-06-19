package de.htwg.se.backgammon.metric

import metric.api.server.MetricHttpServer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object Main {
    @main def run() = {
        MetricHttpServer.run
        Await.result(Future.never, Duration.Inf)
    }
}