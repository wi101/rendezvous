package rendezvous.config

final case class AppConfig(flyway: FlywayConfig, qrCode: QRCodeConfig, endpoint: Endpoint)
