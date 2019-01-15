package chapter06

import akka.actor.{ActorSystem, Props}
import akka.cluster.client.ClusterClientReceptionist
import akka.routing.BalancingPool
import com.typesafe.config.ConfigFactory

/**
  * @Description:
  * @Date 下午4:44 2019/1/15
  * @Author: joker
  */
object Main {

  def start(conf: String) = {
    val system = ActorSystem("Akkademy", ConfigFactory.load(conf))

    val clusterController = system.actorOf(Props[ClusterController], "clusterController")

    val works = system.actorOf(BalancingPool(5).props(Props[ActorParseActor]), "work")

    ClusterClientReceptionist(system).registerService(works)

  }

  def main(args: Array[String]): Unit = {
    start("cluster0.conf")
    Thread.sleep(3000)
    start("cluster1conf")
    start("cluster2.conf")
  }
}
