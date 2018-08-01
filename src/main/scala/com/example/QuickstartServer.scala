package com.example

//#quick-start-server

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.json.JsonSupport

//#main-class
object QuickstartServer extends App with TsvRoutes with JsonSupport {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  import system.dispatcher

  //#http-server
  Http().bindAndHandle(route, "localhost", 8081)

  println(s"Server online at http://localhost:8081/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
