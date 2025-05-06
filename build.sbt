ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"
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
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml"           % "2.2.0",
      "com.typesafe.play"      %% "play-json"           % playJsonVersion,
      "com.typesafe.akka"      %% "akka-actor-typed"    % akkaVersion,
      "com.typesafe.akka"      %% "akka-stream"         % akkaVersion,
      "com.typesafe.akka"      %% "akka-http"           % akkaHttpVersion,
    )
  )

lazy val gameEngine = project
  .in(file("game-engine"))
  .dependsOn(gameCore)
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
      "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion,
    )
  )

lazy val gameUi = project
  .in(file("game-ui"))
  .dependsOn(gameCore)
  .enablePlugins(JavaAppPackaging)
  .settings(
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
  .settings(
    name := "game-storage",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json"        %   playJsonVersion,
      "com.typesafe.akka" %% "akka-actor-typed" %   akkaVersion,
      "com.typesafe.akka" %% "akka-stream"      %   akkaVersion,
      "com.typesafe.akka" %% "akka-http"        %   akkaHttpVersion
    )
  )

