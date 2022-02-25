package rendezvous

import zio.TaskLayer
import zio.config.ConfigDescriptor._
import zio.config.magnolia.DeriveConfigDescriptor
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

package object config {

  type AllConfig = AppConfig with FlywayConfig with QRCodeConfig with Endpoint

  final val Root = "rendezvous"

  private final val Descriptor = DeriveConfigDescriptor.descriptor[AppConfig]

  private val appConfig = TypesafeConfig.fromResourcePath(nested(Root)(Descriptor))

  val live: TaskLayer[AllConfig] =
    appConfig >+>
      appConfig.narrow(_.flyway) >+>
      appConfig.narrow(_.qrCode) >+>
      appConfig.narrow(_.endpoint)

}
