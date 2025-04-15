val scala3Version = "3.3.1"

lazy val gameCore = project
  .in(file("game-core"))
  .settings(
    name := "game-core",
    scalaVersion := scala3Version
  )

lazy val root = project
  .in(file("."))
  .dependsOn(gameCore)
  .aggregate(gameCore)
  .settings(
    name := "backgammon",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "resources",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test",
    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.3",
    coverageExcludedPackages := "<empty>;*view*;*PrettyPrint.scala"
  )