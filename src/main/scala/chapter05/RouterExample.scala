package chapter05

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.routing.{Broadcast, RoundRobinGroup, RoundRobinPool}
import chapter05.akkademy.ArticleParseActor

/**
  * @Description:
  * @Date 下午6:58 2019/1/11
  * @Author: joker
  */
class RouterExample {

  val system = ActorSystem("router-demo")

  //create router by Actor Pool
  val workerRouter: ActorRef = system.actorOf(
    Props.create(classOf[ArticleParseActor])
      .withRouter(new RoundRobinPool(8)
        .withSupervisorStrategy(new OneForOneStrategy(SupervisorStrategy.defaultDecider))//监督 Router Pool 中的路由对象
      )
  )
  //create router by  Actor Group
  val actors = List.empty[ActorRef]
  val router = system.actorOf(
    new RoundRobinGroup(
      actors.map(actor => actor.path.toString)
    ).props()
  )

  val msg = "db is broken"
  router ! Broadcast(msg)

}
