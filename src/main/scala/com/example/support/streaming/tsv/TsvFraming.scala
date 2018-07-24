package com.example.support.streaming.tsv


import akka.NotUsed
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.Flow
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString
import sun.nio.cs.StandardCharsets

object TsvFraming {

  case class IdName(id: String, name: String)

  def idNameScanner(): Flow[ByteString, IdName, NotUsed] =
    Flow[ByteString].via(new GraphStage[FlowShape[ByteString, IdName]] {
      val in = Inlet[ByteString]("TsvFraming.in")
      val out = Outlet[IdName]("TsvFraming.out")

      override val shape = FlowShape.of(in, out)

//      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
//        setHandler(in, new InHandler {
//          private var acc = ""
//          override def onPush(): Unit = {
//            val grabbed: ByteString = grab(in)
//            val grabbedString = grabbed.toString()
//            grabbedString.span(_ != '\n') match {
//              case (left, right) if !right.isEmpty =>
//                val row = acc + left
//                acc = right.tail
//                val splitted = row.split("\t+").toList
//                splitted match {
//                  case id :: name :: Nil =>
//                    push(out, IdName(id, name))
//                }
//              case _ =>
//                acc += grabbedString
//            }
//          }
//        })
//        setHandler(out, new OutHandler {
//          override def onPull(): Unit = {
//            pull(in)
//          }
//        })
//      }
      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val grabbedString = grab(in).decodeString("WINDOWS-1251")
            println(s"grabbedString: ${grabbedString}")
            val splitted = grabbedString.split("\t+").toList
            splitted match {
              case id :: name :: Nil =>
                push(out, IdName(id, name))
              case _ =>
            }
          }
        })
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
      }
    })
}
