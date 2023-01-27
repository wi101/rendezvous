import sbt._
object Dependencies {

  final val Flyway      = "8.5.1"
  final val Zio         = "2.0.6"
  final val ZioConfig   = "3.0.7"
  final val ZioHttp     = "0.0.4"
  final val ZioJson     = "0.4.2"
  final val ZioPrelude  = "1.0.0-RC16"
  final val ZioQuill    = "4.6.0"
  final val PostgresSql = "42.3.2"
  final val QRCode      = "3.4.1"

  val ServerLibs = Seq(
    "dev.zio"         %% "zio"                 % Zio,
    "dev.zio"         %% "zio-config"          % ZioConfig,
    "dev.zio"         %% "zio-config-typesafe" % ZioConfig,
    "dev.zio"         %% "zio-config-magnolia" % ZioConfig,
    "dev.zio"         %% "zio-http"            % ZioHttp,
    "dev.zio"         %% "zio-json"            % ZioJson,
    "dev.zio"         %% "zio-prelude"         % ZioPrelude,
    "io.getquill"     %% "quill-jdbc-zio"      % ZioQuill,
    "org.postgresql"   % "postgresql"          % PostgresSql,
    "com.google.zxing" % "core"                % QRCode,
    "org.flywaydb"     % "flyway-core"         % Flyway
  )
}
