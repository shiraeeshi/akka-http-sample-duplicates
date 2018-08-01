package com.example.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.example.NameWrapper
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object NameWrapperFormat extends RootJsonFormat[NameWrapper] {

    override def read(json: JsValue): NameWrapper = json match {
      case JsString(name) => NameWrapper(name)
      case unexpected => throw DeserializationException(s"cannot parse json: expected JsString, got: $unexpected")
    }

    override def write(obj: NameWrapper): JsValue = JsString(obj.name)

  }

}
