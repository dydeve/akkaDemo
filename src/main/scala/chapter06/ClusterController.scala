package chapter06

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import common.LoggingActor

/**
  * @Description:
  * @Date 下午4:36 2019/1/15
  * @Author: joker
  */
class ClusterController extends LoggingActor{

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    //防止泄露
    cluster.unsubscribe(self)
  }

  override def receive: Receive = {
    case x: MemberEvent => log.info("MemberEvent: {}", x)//该事件会在集群状态发生变化时发出通知
    case x: UnreachableMember => log.info("UnreachableMember {}: ", x)//该事件会在某个节点被标记为不可用时发出通知

  }

  //cluster.leave(self.path.address) 优雅的退出集群
}
