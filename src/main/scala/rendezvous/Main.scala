package rendezvous

import rendezvous.config.{AllConfig, AppConfig}
import rendezvous.db.{DBManager, QuillContext}
import rendezvous.models.Error.DecodeError
import rendezvous.models.{CreateParticipantRequest, Error, Participant}
import rendezvous.service.{ParticipantService, QRCodeService}
import zio.Console.{printLine, readLine}
import zio.http._
import zio.http.model.Method.{GET, POST}
import zio.http.model.{HttpError, Status}
import zio.json.EncoderOps
import zio.{RIO, Task, ZIO, ZIOAppDefault}

import java.net.InetSocketAddress
import java.nio.file.{Path, Paths}

object Main extends ZIOAppDefault {

  type Env = AllConfig with DBManager with ParticipantService with QRCodeService

  val httpApp: App[ParticipantService] =
    Http.collectZIO {
      case req @ POST -> !! / "rendezvous" =>
        httpResponse[ParticipantService, Path](
          for {
            body       <- req.body.asString
            subscriber <- ZIO.fromEither(CreateParticipantRequest.parse(body))
            path       <- ZIO.serviceWithZIO[ParticipantService](_.addParticipant(subscriber))
          } yield path,
          p => Response.text(s"$p").setStatus(Status.Created)
        )

      case request @ GET -> !! / "rendezvous" / "infoQR" if request.url.queryParams.contains("path") =>
        httpResponse[ParticipantService, Participant](
          for {
            p           <- ZIO
                             .fromOption(request.url.queryParams.get("path").flatMap(_.headOption))
                             .orElseFail(DecodeError("invalid path"))
            path        <- makePath(p)
            participant <- ZIO.serviceWithZIO[ParticipantService](_.getParticipantByQRCode(path))
          } yield participant,
          p => Response.json(p.toJson)
        )

      case GET -> !! / "health" => ZIO.succeed(Response.ok)
    }

  override val run =
    (for {
      _        <- QuillContext.migrate
      endpoint <- ZIO.serviceWith[AppConfig](_.endpoint)
      f        <- Server
                    .serve(httpApp.map(_.withAccessControlAllowOrigin("*")))
                    .provideSome[ParticipantService](
                      Server.live,
                      ServerConfig.live(ServerConfig.default.copy(address = new InetSocketAddress(endpoint.port)))
                    )
                    .forkDaemon
      _        <- printLine("Press Any Key to stop the rendezvous server") *> readLine.catchAll(e =>
                    printLine(s"There was an error! ${e.getMessage}")
                  ) *> f.interrupt
    } yield ())
      .provide(config.live, QRCodeService.live, DBManager.live, ParticipantService.live)
      .debug

  private def makePath(path: String): Task[Path] =
    ZIO
      .attempt(Paths.get(path))
      .mapError(e => Error.DecodeError(s"Invalid path: $path. Reason: ${e.getMessage}"))
      .filterOrDie(_.toFile.exists())(Error.NotFound(s"File $path doesn't exist"))

  private def httpResponse[R, A](rio: RIO[R, A], onSuccess: A => Response) =
    rio.fold(
      {
        case Error.DecodeError(msg) => Response.fromHttpError(HttpError.BadRequest(msg))
        case Error.NotFound(msg)    => Response.fromHttpError(HttpError.UnprocessableEntity(msg))
        case e                      => Response.fromHttpError(HttpError.InternalServerError(e.getMessage))
      },
      onSuccess
    )
}
