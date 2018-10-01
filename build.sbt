organization in ThisBuild := "slasch"
version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.7"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test

val defaultDependencies = Seq(lagomScaladslTestKit, macwire, scalaTest)

lazy val executory = (project in file("."))
  .aggregate(
    `shared-model`,
    `scheduler-api`, `scheduler-impl`,
    `executor-api`, `executor-impl`,
    `manager-api`, `manager-impl`)

lazy val `shared-model` = (project in file("shared-model"))
  .settings(libraryDependencies += lagomScaladslApi)

lazy val `scheduler-api` = (project in file("scheduler-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `executor-api` = (project in file("executor-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `manager-api` = (project in file("manager-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `scheduler-impl` = (project in file("scheduler-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      lagomScaladslPubSub,
      macwire
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`scheduler-api`)

lazy val `executor-impl` = (project in file("executor-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= defaultDependencies)
  .dependsOn(`executor-api`)

lazy val `manager-impl` = (project in file("manager-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= defaultDependencies)
  .settings(libraryDependencies += lagomScaladslKafkaBroker)
  .dependsOn(`manager-api`, `scheduler-api`, `executor-api`)

