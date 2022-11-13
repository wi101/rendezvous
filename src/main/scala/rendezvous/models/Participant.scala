package rendezvous.models

import io.getquill.MappedEncoding
import rendezvous.models.Error.DecodeError
import zio.json.{DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.prelude.{Assertion, Newtype}
import zio.{Random, ZIO}

import java.util.UUID
import scala.util.Try

object ParticipantId extends Newtype[UUID] {
  def create: ZIO[Random, Nothing, ParticipantId] =
    ZIO.serviceWithZIO[Random] { random =>
      random.nextUUID.map(ParticipantId.wrap)
    }

  def parse(id: String): Either[DecodeError, ParticipantId] =
    for {
      uuid          <- Try(UUID.fromString(id)).toEither.left.map(e => DecodeError(e.getMessage))
      participantId <- ParticipantId
                         .make(uuid)
                         .toEitherWith { _ =>
                           DecodeError(s"Invalid participant id: $id")
                         }
    } yield participantId

  implicit val encoder: JsonEncoder[ParticipantId] = JsonEncoder[UUID].contramap(ParticipantId.unwrap)
  implicit val decoder: JsonDecoder[ParticipantId] = JsonDecoder[UUID]
    .mapOrFail(id => ParticipantId.make(id).toEitherWith(_ => s"Invalid participant id: $id"))

  implicit val encoderDBId = MappedEncoding[ParticipantId, String](s => ParticipantId.unwrap(s).toString)
  implicit val decoderDBId =
    MappedEncoding[UUID, ParticipantId](id => ParticipantId.make(id).getOrElse(throw DecodeError(s"Invalid id: $id")))

}
object Email extends Newtype[String] {
  private final val regex =
    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])".r
  override def assertion  = assert(Assertion.matches(regex))

  def parse(email: String): Either[DecodeError, Email] =
    Email
      .make(email)
      .toEitherWith { _ =>
        DecodeError(s"Invalid email: $email")
      }

  implicit val encoder: JsonEncoder[Email] = JsonEncoder[String].contramap(Email.unwrap)
  implicit val decoder: JsonDecoder[Email] = JsonDecoder[String]
    .mapOrFail(email => Email.make(email).toEitherWith(_ => s"Invalid email: $email"))

  implicit val encoderDBEmail = MappedEncoding[Email, String](s => Email.unwrap(s))
  implicit val decoderDBEmail =
    MappedEncoding[String, Email](email => Email.make(email).getOrElse(throw DecodeError(s"Invalid email: $email")))
}

object Name extends Newtype[String] {
  private final val regex = "^[\\p{L} ]+$".r
  override def assertion  = assert(
    Assertion.hasLength(Assertion.greaterThan(2)) && Assertion.matches(regex)
  )

  implicit val encoder: JsonEncoder[Name] = JsonEncoder[String].contramap(Name.unwrap)
  implicit val decoder: JsonDecoder[Name] = JsonDecoder[String]
    .mapOrFail(name => Name.make(name).toEitherWith(_ => s"Invalid name: '$name'"))

  implicit val encoderDBName = MappedEncoding[Name, String](s => Name.unwrap(s))
  implicit val decoderDBName =
    MappedEncoding[String, Name](name => Name.make(name).getOrElse(throw DecodeError(s"Invalid name: '$name''")))
}

final case class Participant(id: ParticipantId, name: Name, email: Email)
object Participant {
  def createFromRequest(pendingParticipant: CreateParticipantRequest): ZIO[Random, Nothing, Participant] =
    ParticipantId.create.map(Participant(_, pendingParticipant.name, pendingParticipant.email))

  implicit val encoder: JsonEncoder[Participant] = DeriveJsonEncoder.gen[Participant]
}
