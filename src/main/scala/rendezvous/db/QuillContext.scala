package rendezvous.db

import io.getquill.jdbczio.Quill
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import org.flywaydb.core.Flyway
import rendezvous.config.FlywayConfig
import zio.{RIO, ULayer, ZIO}

import javax.sql.DataSource

object QuillContext extends PostgresZioJdbcContext(SnakeCase) {
  val dataSourceLayer: ULayer[DataSource] =
    Quill.DataSource.fromPrefix("postgres").orDie

  def migrate: RIO[FlywayConfig, Unit] =
    ZIO.serviceWithZIO[FlywayConfig] { config =>
      for {
        flyway <- ZIO.attempt(Flyway.configure().dataSource(config.url, config.username, config.password).load())
        _      <- ZIO.attempt(flyway.migrate())
      } yield ()
    }
}
