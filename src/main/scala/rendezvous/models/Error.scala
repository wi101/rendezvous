package rendezvous.models

sealed trait Error extends Exception

object Error {
  case class DecodeError(str: String)               extends Error
  final case class InternalServerError(msg: String) extends Error
  final case class NotFound(msg: String)            extends Error
}
