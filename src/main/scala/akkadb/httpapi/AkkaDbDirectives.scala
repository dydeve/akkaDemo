package akkadb.httpapi

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.optionalHeaderValueByType
import akka.http.scaladsl.server.Directives.provide

/**
  * @Description:
  * @Date 下午1:57 2019/1/28
  * @Author: joker
  */
trait AkkaDbDirectives {

  def withVectorClockHeader: Directive1[VectorClockHeader] = {
    optionalHeaderValueByType[VectorClockHeader]((): Unit).flatMap {
      case Some(header) => provide(header)
      case None => provide(VectorClockHeader.empty)
    }
  }

}

object AkkaDbDirectives extends AkkaDbDirectives
