package com.example

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import com.example.json.JsonSupport
import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

class TsvRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with TsvRoutes with JsonSupport {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(12.seconds)

  "TSV routes" should {
    "return duplicates for two files correctly" in {
      val file1 = new File(getClass.getResource("/second_test_1.txt").getPath)
      val file2 = new File(getClass.getResource("/second_test_2.txt").getPath)
      HttpCharsets.getForKey("windows-1251") match {
        case Some(charset) =>
          val bodyPart1 = Multipart.FormData.BodyPart.fromFile("ids-to-names", MediaTypes.`text/tab-separated-values` withCharset charset, file1)
          val bodyPart2 = Multipart.FormData.BodyPart.fromFile("ids-to-names-2", MediaTypes.`text/tab-separated-values` withCharset charset, file2)
          val formData = Multipart.FormData(bodyPart2, bodyPart1)
          Post("/second", formData) ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Map[String, (Set[String], Set[String])]] shouldBe
              Map(
                "пользователь" -> (Set("111"), Set("111")),
                "администратор" -> (Set("112"), Set("112")),
                "директор" -> (Set("113", "1132"), Set("113"))
              )

          }
        case _ =>
          fail("windows-1251 charset is not supported in this environment")
      }
    }
    "return duplicates for single file correctly" in {
      val file = new File(getClass.getResource("/testFile.txt").getPath)
      HttpCharsets.getForKey("windows-1251") match {
        case Some(charset) =>
          val formData = Multipart.FormData.fromFile("ids-to-names", MediaTypes.`text/tab-separated-values` withCharset charset, file)
          Post("/first", formData) ~> route ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Map[String, Set[String]]] shouldBe
              Map(
                "бухгалтер" -> Set("114", "1142"),
                "директор" -> Set("113", "1132")
              )
          }
        case _ =>
          fail("windows-1251 charset is not supported in this environment")
      }
    }
  }
}
