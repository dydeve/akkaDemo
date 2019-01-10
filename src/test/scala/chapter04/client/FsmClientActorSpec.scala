package chapter04.client

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}
import scala.concurrent.duration._

/**
  * @Description:
  * @Date 上午12:15 2019/1/11
  * @Author: joker
  */
class FsmClientActorSpec extends FunSpec with Matchers {

  implicit val system = ActorSystem("test-system", ConfigFactory.defaultReference())
  implicit val timeout = Timeout(5 second)

}
