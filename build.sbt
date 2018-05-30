lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "scalable",
      scalaVersion := "2.12.6",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "reactive-trade-analyser",
    libraryDependencies ++= Seq(
      "io.monix"        %% "monix"      % "2.3.3",
      "io.monix"        %% "monix-cats" % "2.3.3",
      "org.typelevel"   %% "cats"       % "0.9.0",
      "com.nrinaudo"    %% "kantan.csv" % "0.4.0",
      "org.scalatest"   %% "scalatest"  % "3.0.5"   % "test"
    )
  )
