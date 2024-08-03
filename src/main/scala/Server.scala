import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

@main def main(): Unit = {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "ChatServer")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val route =
    path("") {
      get {
        complete(
          "Hello, World!"
        )
      }
    }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}