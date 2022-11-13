package rendezvous
package service

import com.google.zxing._
import com.google.zxing.common.{BitMatrix, GlobalHistogramBinarizer}
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.{QRCodeReader, QRCodeWriter}
import rendezvous.config.QRCodeConfig
import rendezvous.models.ParticipantId
import zio.{ZIO, Task, ZLayer}

import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.{Path, Paths}
import javax.imageio.ImageIO
import scala.jdk.CollectionConverters.MapHasAsJava

trait QRCodeService {
  def generateFor(id: ParticipantId): Task[Path]
  def readFrom(path: Path): Task[Option[ParticipantId]]
}

object QRCodeService {

  val live: ZLayer[QRCodeConfig, Nothing, QRCodeService] = ZLayer.fromFunction(service _)

  private def service(config: QRCodeConfig)   = new QRCodeService {
    override def generateFor(id: ParticipantId): Task[Path] = {
      val writer = new QRCodeWriter
      val hints  = Map(
        EncodeHintType.CHARACTER_SET    -> "UTF-8",
        EncodeHintType.MARGIN           -> 1,
        EncodeHintType.ERROR_CORRECTION -> ErrorCorrectionLevel.H
      ).asJava

      (for {
        bitMatrix <- ZIO.attempt(writer.encode(id.toString, BarcodeFormat.QR_CODE, config.width, config.height, hints))
        image     <- makeImage(bitMatrix)
        filePath  <- ZIO.attempt(Paths.get(s"${config.pathPrefix}/$id.png"))
        _         <- ZIO.attempt(ImageIO.write(image, "png", filePath.toFile))
      } yield filePath)
        .debug("generate QR code")
    }

    override def readFrom(path: Path): Task[Option[ParticipantId]] = {
      val hints = Map(
        DecodeHintType.PURE_BARCODE     -> java.lang.Boolean.TRUE,
        DecodeHintType.POSSIBLE_FORMATS -> List(BarcodeFormat.QR_CODE)
      ).asJava

      for {
        img     <- ZIO.attempt(ImageIO.read(path.toFile))
        w        = img.getWidth
        h        = img.getHeight
        pixels   = new Array[Int](w * h)
        _       <- ZIO.attempt(img.getRGB(0, 0, w, h, pixels, 0, w))
        source   = new RGBLuminanceSource(img.getWidth, img.getHeight, pixels)
        bitmap  <- ZIO.attempt(new BinaryBitmap(new GlobalHistogramBinarizer(source)))
        reader   = new QRCodeReader()
        uuidStr <- ZIO.attempt(reader.decode(bitmap, hints).getText)
        result  <- ZIO.fromEither(ParticipantId.parse(uuidStr))
      } yield result
    }.option
      .debug("read from QR code")

  }
  private def makeImage(bitMatrix: BitMatrix) = ZIO.attempt {
    val idWidth    = bitMatrix.getWidth
    val image      = new BufferedImage(idWidth, idWidth, BufferedImage.TYPE_BYTE_BINARY)
    val graphics2D = image.createGraphics()
    graphics2D.setColor(Color.WHITE)
    graphics2D.fillRect(0, 0, idWidth, idWidth)
    graphics2D.setColor(Color.BLACK)
    for (i <- 0 until idWidth)
      for (j <- 0 until idWidth)
        if (bitMatrix.get(i, j)) graphics2D.fillRect(i, j, 1, 1)
    image
  }
}
