package akkadb.httpapi

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

/**
  * @Description:
  * @Date 下午3:33 2019/1/27
  * @Author: joker
  */
class BuildInfoRouter {

  def route(buildInfoJson: String): Route = path("info") {
    get {
      complete(HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), buildInfoJson)))
    }
  }

}
