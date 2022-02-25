package rendezvous.service

import rendezvous.db.DBManager
import rendezvous.models._
import zio._

import java.nio.file.Path
import zio.Random

trait ParticipantService {
  def addParticipant(request: CreateParticipantRequest): Task[Path]
  def getParticipantByQRCode(path: Path): Task[Participant]
}

object ParticipantService extends Accessible[ParticipantService] {

  val live: URLayer[DBManager with QRCodeService with Random, ParticipantService] = (service _).toLayer

  private def service(db: DBManager, qRCodeService: QRCodeService, random: Random) = new ParticipantService {
    override def addParticipant(request: CreateParticipantRequest): Task[Path] =
      for {
        participant <- Participant.createFromRequest(request).provide(ZLayer.succeed(random))
        _           <- db.save(participant)
        path        <- qRCodeService.generateFor(participant.id)
      } yield path

    override def getParticipantByQRCode(path: Path): Task[Participant] =
      qRCodeService
        .readFrom(path)
        .flatMap(_.fold[Task[Participant]](Task.fail(Error.NotFound("Participant is not found")))(db.getById))
  }

}
