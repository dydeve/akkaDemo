package chapter05

import akka.actor.{ActorSystem, Props}
import akka.routing.{RoundRobinGroup, RoundRobinPool}
import chapter05.TestHelper.TestCameoActor
import chapter05.akkademy.{ArticleParseActor, ParseArticle}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Promise

/**
  * @Description:
  * @Date 上午1:56 2019/1/14
  * @Author: joker
  */
class AssignActorsToDispatcherTest extends FlatSpec with Matchers {

  val system = ActorSystem("assignActorToDispatcher", ConfigFactory.load("dispatcher.conf"))

  "ActorsAssignedToDispatcher" should "do work concurrently" in {
    val p = Promise[String]()

    val actors = (0 to 7).map { x =>
      system.actorOf(Props(classOf[ArticleParseActor])
        .withDispatcher("article-parsing-dispatcher")
      )

    }.toList

    //create by group
    val workRouter = system.actorOf(
      RoundRobinGroup(actors.map(x => x.path.toStringWithoutAddress)).props(),
      "workerRouter"
    )

    //create by pool
    //system.actorOf(Props.create(classOf[ArticleParseActor]).withDispatcher("article-parsing-dispatcher").withRouter(new RoundRobinPool(8)), "workerRouter")

    val cameoActor = system.actorOf(Props(new TestCameoActor(p)))

    (0 to 2000).foreach(x => {
      workRouter.tell(
        new ParseArticle(TestHelper.file),
        cameoActor
      )
    })
  }
}
