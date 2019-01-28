package akkadb.core.db.cluster

import akkadb.consistenthashing.NodeId
import akkadb.core.db.actor.StorageNodeActorRef

/**
  * @Description:
  * @Date 下午2:11 2019/1/28
  * @Author: joker
  */
case class ClusterMembers(private val members: Map[NodeId, StorageNodeActorRef]) {

  def contains(nodeId: NodeId): Boolean = members.contains(nodeId)
  def notContains(nodeId: NodeId): Boolean = !contains(nodeId)

  def add(nodeId: NodeId, ref: StorageNodeActorRef): ClusterMembers = {
    ClusterMembers(this.members + (nodeId -> ref))
  }

  def get(nodeId: NodeId): Option[StorageNodeActorRef] = members.get(nodeId)

  def removeByRef(ref: StorageNodeActorRef): ClusterMembers = {
    val filteredMembers = members.filterNot{ case (_, sRef) => sRef == ref}
    ClusterMembers(filteredMembers)
  }

  def size: Int = members.size

  override def toString: String = members.toString()

}

object ClusterMembers {
  def empty: ClusterMembers = ClusterMembers(Map.empty[NodeId, StorageNodeActorRef])
}