val scala3Version = "3.3.1"

lazy val gameCore = project
  .in(file("."))
  .settings(
    name := "game-core",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "com.typesafe.play" %% "play-json" % "2.10.3"
    )
  )