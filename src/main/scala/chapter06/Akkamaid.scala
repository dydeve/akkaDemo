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
    //akka.tcp://Akkademy@127.0.0.1:2551,2552,2553
    val system = ActorSystem("Akkademy", ConfigFactory.load(conf))

    val clusterController = system.actorOf(Props[ClusterController], "clusterController")

    val works = system.actorOf(BalancingPool(5).props(Props[ActorParseActor]), "work")

    ClusterClientReceptionist(system).registerService(works)

  }
}

object Main0 {
  def main(args: Array[String]): Unit = {
    Main.start("cluster0.conf")
  }
}

object Main1 {
  def main(args: Array[String]): Unit = {
    Main.start("cluster1.conf")
  }
}

object Main2 {
  def main(args: Array[String]): Unit = {
    Main.start("cluster2.conf")
  }
}
