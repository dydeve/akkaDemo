package akkadb.httpapi

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

/**
  * @Description:
  * @Date 下午3:44 2019/1/27
  * @Author: joker
  */
class HealthCheckRouter {

  val routes: Route = path("health") {
    get { complete(StatusCodes.OK) }
  }

}
