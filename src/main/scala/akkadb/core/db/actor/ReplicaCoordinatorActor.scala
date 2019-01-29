package akkadb.core.db.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.{DefaultResizer, RoundRobinPool}
import akkadb.core.db.actor.protocol.{ReadData, WriteData}

import scala.concurrent.ExecutionContext

/**
  * @Description:
  * @Date 上午12:16 2019/1/29
  * @Author: joker
  */
class ReplicaCoordinatorActor(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator) extends Actor with ActorLogging {
  private implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case rd: ReadData => readCoordinator.apply(rd.cmd, rd.clusterMembers).foreach(rd.sender ! _)
    case wd: WriteData => writeCoordinator.apply(wd.cmd, wd.clusterMembers).foreach(wd.sender ! _)
  }
}

object ReplicaCoordinatorActor {
  def props(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator): Props = {
    Props(new ReplicaCoordinatorActor(readCoordinator, writeCoordinator))
  }
}

object RoundRobinCoordinatorRouter {
  def routerName: String = "CoordinatorRouter"

  private val pool = RoundRobinPool(
    nrOfInstances = 5,
    resizer = Some(DefaultResizer(lowerBound = 2, upperBound = 15))
  )

  def props(readCoordinator: ReplicaReadCoordinator, writeCoordinator: ReplicaWriteCoordinator): Props = {
    pool.props(ReplicaCoordinatorActor.props(readCoordinator, writeCoordinator))
  }
}
