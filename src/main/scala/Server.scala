import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol.*
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

case class Message(message: String)

@main def main(): Unit = {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "ChatServer")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat1(Message.apply)
  val logger = LoggerFactory.getLogger("ChatServerLogger")

  val routes =
    pathSingleSlash {
      logger.info("GET /")
      getFromResource("public/index.html")
    } ~
    path("send-message") {
      post {
        entity(as[Message]) { message =>
          logger.info(s"POST / - Received message: ${message.message}")
          complete(StatusCodes.OK, Message.apply("Message received"))
        }
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 7999).bind(routes)
  logger.info(s"Server online at http://localhost:7999/")
  logger.debug("Press RETURN to stop...")

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ =>
      logger.info("Server stopped")
      system.terminate())
}
