package rendezvous.models

import rendezvous.models.Error.DecodeError
import zio.json._

final case class CreateParticipantRequest(name: Name, email: Email)

object CreateParticipantRequest {
  def parse(str: String): Either[DecodeError, CreateParticipantRequest] =
    str.fromJson[CreateParticipantRequest].left.map(e => Error.DecodeError(e))

  implicit val decoder: JsonDecoder[CreateParticipantRequest] = DeriveJsonDecoder.gen[CreateParticipantRequest]
}
