import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "ffm.trader",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "ffm-trader",
    libraryDependencies ++= Seq(
     "io.monix" %% "monix-reactive" % "2.3.3", 
      scalaTest % Test
    )
  )
