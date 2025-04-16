ThisBuild / organization := "de.htwg.se.backgammon"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.5.0"

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
  .dependsOn(gameCore, gameEngine, gameUi)
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
    name := "game-core",
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
      "com.typesafe.play" %% "play-json" % "2.10.3"
    )
  )

lazy val gameEngine = project
  .in(file("game-engine"))
  .dependsOn(gameCore)
  .settings(
    name := "game-engine"
  )

lazy val gameUi = project
  .in(file("game-ui"))
  .dependsOn(gameCore)
  .dependsOn(gameEngine)
  .settings(
    name := "game-ui",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "21.0.0-R32"
    )
  )
