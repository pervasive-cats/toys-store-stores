import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / excludeLintKeys := Set(idePackagePrefix)

ThisBuild / scalaVersion := "3.2.2"

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.liancheng" %% "organize-imports" % "0.6.0",
  "io.github.ghostbuster91.scalafix-unified" %% "unified" % "0.0.8",
  "net.pixiv" %% "scalafix-pixiv-rule" % "4.1.0"
)

ThisBuild / idePackagePrefix := Some("io.github.pervasivecats")

lazy val root = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "toys-store-stores",
    scalacOptions ++= Seq(
      "-deprecation",
      "-Xfatal-warnings"
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    libraryDependencies ++= Seq(
      scalactic,
      scalatest,
      refined,
      akka,
      akkaStream,
      akkaHttp,
      akkaHttpSprayJson,
      akkaTestKit,
      rabbitMQ,
      akkaStreamTestkit,
      akkaHttpTestkit,
      testContainers,
      postgresql,
      testContainersPostgresql,
      ditto,
      quill,
      enumeratum
    ),
    wartremoverErrors ++= Warts.allBut(Wart.ImplicitParameter),
    version := "1.0.0",
    coverageMinimumStmtTotal := 80,
    coverageMinimumBranchTotal := 80,
    headerLicense := Some(
      HeaderLicense.Custom(
        """|Copyright © 2022-2023 by Pervasive Cats S.r.l.s.
           |
           |All Rights Reserved.
           |""".stripMargin
      )
    ),
    assembly / assemblyJarName := "main.jar",
    assembly / mainClass := Some("io.github.pervasivecats.main"),
    assembly / assemblyMergeStrategy := {
      case PathList("io", "getquill", _*) => MergeStrategy.first
      case PathList("META-INF", "annotations", _*) => MergeStrategy.filterDistinctLines
      case PathList("META-INF", "NOTICE-THIRD-PARTY.md") => MergeStrategy.first
      case v => MergeStrategy.defaultMergeStrategy(v)
    }
  )
