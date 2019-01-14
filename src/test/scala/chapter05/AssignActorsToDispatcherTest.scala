package chapter05

import akka.actor.{ActorSystem, Props}
import akka.routing.{RoundRobinGroup}
import chapter05.TestHelper.TestCameoActor
import chapter05.akkademy.{ArticleParseActor, ParseArticle}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

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
      RoundRobinGroup(actors.map(x => {
        /*
        println(x.path.toString)
        println(x.path.toStringWithoutAddress)
        akka://assignActorToDispatcher/user/$a
        /user/$a
        */
        x.path.toStringWithoutAddress})).props(),
      "workerRouter"
    )

    //create by pool
    //system.actorOf(Props.create(classOf[ArticleParseActor]).withDispatcher("article-parsing-dispatcher").withRouter(new RoundRobinPool(8)), "workerRouter")

    val cameoActor = system.actorOf(Props(new TestCameoActor(p)))

    (0 to 16).foreach(x => {
      workRouter.tell(
        new ParseArticle(TestHelper.file),
        cameoActor
      )
    })

    TestHelper.profile(() => Await.ready(p.future, 20 seconds), "ActorsAssignedToDispatcher")
  }
}
