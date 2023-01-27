package rendezvous.service

import rendezvous.db.DBManager
import rendezvous.models._
import zio._

import java.nio.file.Path

trait ParticipantService {
  def addParticipant(request: CreateParticipantRequest): Task[Path]
  def getParticipantByQRCode(path: Path): Task[Participant]
}

object ParticipantService {
  private def service(db: DBManager, qRCodeService: QRCodeService) = new ParticipantService {
    override def addParticipant(request: CreateParticipantRequest): Task[Path] =
      for {
        participant <- Participant.createFromRequest(request)
        _           <- db.save(participant)
        path        <- qRCodeService.generateFor(participant.id)
      } yield path

    override def getParticipantByQRCode(path: Path): Task[Participant] =
      qRCodeService
        .readFrom(path)
        .flatMap(_.fold[Task[Participant]](ZIO.fail(Error.NotFound("Participant is not found")))(db.getById))
  }

  val live: URLayer[DBManager with QRCodeService, ParticipantService] = ZLayer.fromFunction(service _)

}
