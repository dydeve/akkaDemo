package chapter05

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.BalancingPool
import chapter05.TestHelper.TestCameoActor
import chapter05.akkademy.{ArticleParseActor, ParseArticle}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

/**
  * @Description:
  * @Date 下午5:25 2019/1/14
  * @Author: joker
  */
class BalancingPoolSpec extends FlatSpec with Matchers {

  val system = ActorSystem("assignActorToDispatcher", ConfigFactory.load("dispatcher.conf"))

  "BalancingPool" should "do work concurrently" in {
    val p = Promise[String]()

    val workRouter = system.actorOf(BalancingPool(8).props(Props(classOf[ArticleParseActor])), "balancing-pool-router")

    val cameoActor: ActorRef = system.actorOf(Props(new TestCameoActor(p)))

    (0 to 2000).foreach(x => {
      workRouter.tell(new ParseArticle(TestHelper.file), cameoActor)
    }
    )

    TestHelper.profile(() => Await.ready(p.future, 20 seconds), "ActorsInBalacingPool")
  }

}
