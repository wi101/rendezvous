name := "rendezvous"

ThisBuild / scalaVersion := "2.13.6"

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

inThisBuild(
  Seq(
    scalafmtOnCompile := true
  )
)

lazy val server = project
  .in(file("."))
  .settings(libraryDependencies := Dependencies.ServerLibs)
