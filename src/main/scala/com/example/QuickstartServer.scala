package com.example

//#quick-start-server
import akka.NotUsed

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol


//#main-class
object QuickstartServer extends App with TsvRoutes with SprayJsonSupport with DefaultJsonProtocol {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  //#main-class
  // from the UserRoutes trait
  // lazy val routes: Route = userRoutes
  //#main-class

  import system.dispatcher


  //#http-server
  //Http().bindAndHandle(routes, "localhost", 8080)
  //Http().bindAndHandleAsync(handler, "localhost", 8081)
  Http().bindAndHandle(route, "localhost", 8081)

  println(s"Server online at http://localhost:8081/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
