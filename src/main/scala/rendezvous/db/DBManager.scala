package rendezvous
package db

import rendezvous.models.{Error, Participant, ParticipantId}
import zio.{Task, ULayer, ZLayer}

trait DBManager {
  def save(participant: Participant): Task[Unit]
  def getById(id: ParticipantId): Task[Participant]
}

object DBManager {

  val live: ULayer[DBManager] = ZLayer.succeed {
    new DBManager {

      import rendezvous.db.QuillContext._

      override def save(participant: Participant): Task[Unit] =
        run(query[Participant].insertValue(lift(participant))).unit
          .provide(dataSourceLayer)
          .mapError(e => Error.InternalServerError(e.getMessage))

      override def getById(id: ParticipantId): Task[Participant] =
        run(query[Participant].filter(_.id == lift(id)))
          .map(_.headOption)
          .provide(dataSourceLayer)
          .mapError(e => Error.InternalServerError(e.getMessage))
          .collect(Error.NotFound("Participant is not found")) { case Some(a) => a }

    }
  }

}
