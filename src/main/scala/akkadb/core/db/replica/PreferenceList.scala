package akkadb.core.db.replica

import akkadb.consistenthashing.{NodeId, Ring}
import akkadb.storage.api.RingPartitionId

/**
  * @Description:
  * @Date 下午5:38 2019/1/28
  * @Author: joker
  */
case class PreferenceList(primaryNodeId: NodeId, replicasNodeId: List[NodeId]) {
  def size: Int = all.size
  def all: List[NodeId] = primaryNodeId :: replicasNodeId
}

object PreferenceList {

  def apply(baseRingPartitionId: RingPartitionId, n: N, ring: Ring): Either[Error, PreferenceList] = {
    ring.getNodeId(baseRingPartitionId) match {
      case Some(coordinatorNodeId) => computePreferenceList(baseRingPartitionId, coordinatorNodeId, n, ring)
      case None => Left(LackOfCoordinator)
    }
  }

  private def computePreferenceList(baseRingPartitionId: RingPartitionId, coordinatorNodeId: NodeId, n: N, ring: Ring) = {
    val maxPartitionId = baseRingPartitionId + n.n -1
    val restNodesIds = (baseRingPartitionId until maxPartitionId).map(getNextNodeId(ring)).flatten.distinct.filterNot(_ == coordinatorNodeId)
    val preferenceList = PreferenceList(coordinatorNodeId, restNodesIds.toList)

    if (preferenceList.size >= n.n) {
      Right(preferenceList)
    } else {
      Left(NotSufficientSize(preferenceList))
    }
  }

  //private def getNextNodeId(ring: Ring) = ring.nextPartitionId _ andThen ring.getNodeId _
  private def getNextNodeId(ring: Ring) = ring.nextPartitionId _ andThen ring.getNodeId _

  sealed trait Error
  case object LackOfCoordinator extends Error
  case class NotSufficientSize(preferenceList: PreferenceList) extends Error
}
