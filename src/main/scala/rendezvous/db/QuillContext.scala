package rendezvous.db

import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import org.flywaydb.core.Flyway
import rendezvous.config.FlywayConfig
import zio.{RIO, Task, ULayer, ZIO}

import javax.sql.DataSource

object QuillContext extends PostgresZioJdbcContext(SnakeCase) {
  val dataSourceLayer: ULayer[DataSource] =
    DataSourceLayer.fromPrefix("postgres").orDie

  def migrate: RIO[FlywayConfig, Unit] =
    ZIO.serviceWithZIO[FlywayConfig] { config =>
      for {
        flyway <- Task(Flyway.configure().dataSource(config.url, config.username, config.password).load())
        _      <- Task(flyway.migrate())
      } yield ()
    }
}
