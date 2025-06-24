import sbtassembly.MergeStrategy

def mergeStrategy(x: String): MergeStrategy = x match {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"
ThisBuild / assemblyMergeStrategy := mergeStrategy
ThisBuild / organization := "de.htwg.se.backgammon"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.5.0"

val akkaVersion            = "2.8.5"
val akkaHttpVersion        = "10.5.3"
val playJsonVersion        = "2.10.3"
val playJsonSupportVersion = "1.39.2"

lazy val root = project
  .in(file("."))
  .aggregate(game)
  .dependsOn(game)
  .settings(
    name := "backgammon",
    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "resources",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scalafx" %% "scalafx" % "21.0.0-R32"
    )
  )

lazy val game = project
  .in(file("game"))
  .dependsOn(gameCore, gameEngine, gameUi, gameStorage)
  .settings(
    name := "game",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalactic" %% "scalactic" % "3.2.14",
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "org.scalafx" %% "scalafx" % "21.0.0-R32"
    )
  )

lazy val gameCore = project
  .in(file("game-core"))
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml"           % "2.2.0",
      "com.typesafe.play"      %% "play-json"           % playJsonVersion,
      "com.typesafe.akka"      %% "akka-actor-typed"    % akkaVersion,
      "com.typesafe.akka"      %% "akka-stream"         % akkaVersion,
      "com.typesafe.akka"      %% "akka-http"           % akkaHttpVersion,
      "com.typesafe.slick"     %% "slick"               % "3.5.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4"   
    ),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.mavenCentral
    )
  )

lazy val gameEngine = project
  .in(file("game-engine"))
  .dependsOn(gameCore)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "engine",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed"   % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"        % akkaVersion,
      "com.typesafe.akka" %% "akka-http"          % akkaHttpVersion, 
      "org.apache.kafka"  %  "kafka-clients"      %   "3.7.0",
      "com.typesafe.akka" %% "akka-stream-kafka"  %   "4.0.2"
    )
  )

lazy val gameUi = project
  .in(file("game-ui"))
  .dependsOn(gameCore)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "ui",
    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "main" / "resources",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32",     
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
      "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion
    )
  )

lazy val gameStorage = project
  .in(file("game-storage"))
  .dependsOn(gameCore)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "storage",
    libraryDependencies ++= Seq(
      "org.mongodb.scala" %% "mongo-scala-driver" % "5.4.0" cross CrossVersion.for3Use2_13,
      "com.typesafe.slick"     %% "slick"               % "3.5.0",
      "com.typesafe.play" %% "play-json"        %   playJsonVersion,
      "com.typesafe.akka" %% "akka-actor-typed" %   akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      %   akkaVersion,
      "com.typesafe.akka" %% "akka-http"        %   akkaHttpVersion,
      "org.apache.kafka"  %  "kafka-clients"      %   "3.7.0",
      "com.typesafe.akka" %% "akka-stream-kafka"  %   "4.0.2"
    )
  )

lazy val metric = project
  .in(file("metric"))
  .dependsOn(gameCore, gameStorage)
  .enablePlugins(GatlingPlugin)
  .settings(
    name := "metric",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.3",  
      "org.postgresql" % "postgresql" % "42.7.3",
      "com.typesafe.slick"     %% "slick"               % "3.5.0",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
      "org.mongodb.scala" %% "mongo-scala-driver" % "5.4.0" cross CrossVersion.for3Use2_13,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.13.5" % Test,
      "io.gatling"            % "gatling-test-framework"    % "3.13.5" % Test
    )
  )

  lazy val eventRelay = project
    .in(file("event-relay"))
    .settings(
      name := "event-relay",
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play-json"          %   playJsonVersion,
        "com.typesafe.akka" %% "akka-actor-typed"   %   akkaVersion,
        "com.typesafe.akka" %% "akka-stream"        %   akkaVersion,
        "com.typesafe.akka" %% "akka-http"          %   akkaHttpVersion,
        "org.apache.kafka"  % "kafka-clients"       %   "3.7.0",
        "com.typesafe.akka" %% "akka-stream-kafka"  %   "4.0.2"
      )
    )