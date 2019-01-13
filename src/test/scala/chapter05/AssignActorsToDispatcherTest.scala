package chapter05

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinGroup
import chapter05.akkademy.ArticleParseActor
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Promise

/**
  * @Description:
  * @Date 上午1:56 2019/1/14
  * @Author: joker
  */
class AssignActorsToDispatcherTest extends FlatSpec with Matchers {

  val system = ActorSystem("", ConfigFactory.load("dispatcher.conf"))

  "ActorsAssignedToDispatcher" should "do work concurrently" in {
    val p = Promise[String]()

    val actors = (0 to 7).map{x =>
      system.actorOf(Props(classOf[ArticleParseActor]).withDispatcher("article-parsing-dispatcher"))

    }.toList

    val workRouter = system.actorOf(RoundRobinGroup(
      actors.map(x => x.path.toStringWithoutAddress)).props(), "workerRouter")
  }
}
