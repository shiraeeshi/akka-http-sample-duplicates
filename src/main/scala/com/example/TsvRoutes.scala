package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.example.support.streaming.tsv.TsvFraming
import com.example.support.streaming.tsv.TsvFraming.IdName
import spray.json.DefaultJsonProtocol

import scala.collection.immutable.HashSet
import scala.concurrent.Future

trait TsvRoutes { this: SprayJsonSupport with DefaultJsonProtocol =>

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  import system.dispatcher

  lazy val route = concat(
    path("first") {
      post {
        fileUpload("ids-to-names") {
          case (_, bytesSource) =>
            val f = toIdsByNamesMap(bytesSource)
            val fdups = f map { m =>
              m.filter { case (name, set) => set.size > 1 }
            }
            complete(fdups)
        }
      }
    },
    path("second") {
      post {
        fileUpload("ids-to-names") {
          case (_, bytesSource) =>
            val f = toIdsByNamesMap(bytesSource)
            fileUpload("ids-to-names-2") {
              case (_, bytesSource2) => {
                val f2 = toIdsByNamesMap(bytesSource2)
                val fpair = for {
                  n1 <- f
                  n2 <- f2
                } yield {
                  val dupsKeys = n1.keySet.intersect(n2.keySet)
                  val result = dupsKeys.map( key =>
                    key -> (n1(key), n2(key))
                  ).toMap
                  result
                }
                complete(fpair)
              }
            }
        }
      }
    }
  )
  def toIdsByNamesMap(bytesSource: Source[ByteString, Any]): Future[Map[String, Set[String]]] = {
    val idNamesSource = bytesSource
      .via(Framing.delimiter(
        ByteString("\n"),
        maximumFrameLength = 256,
        allowTruncation = true))
      .via(TsvFraming.idNameScanner())
    collectToMap(idNamesSource)
  }
  def collectToMap(source: Source[IdName, Any]): Future[Map[String, Set[String]]] = {
    source
      .runFold(Map.empty[String, Set[String]]) { (m: Map[String, Set[String]], idName: IdName) => {
        m.find { case (name, set) => name == idName.name} match {
          case Some((name, set)) =>
            m + (name -> (set + idName.id))
          case _ =>
            m + (idName.name -> HashSet(idName.id))
        }
      }}
  }

}
