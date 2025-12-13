val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "backgammon",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalaJSUseMainModuleInitializer := false,
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule)),
    scalaJSLinkerConfig ~= (_.withSourceMap(false)),

    // Optimize for production
    scalaJSStage in Compile := FullOptStage,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test",
    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.2.0",
    coverageExcludedPackages := "<empty>;*view*;*PrettyPrint.scala"
  )