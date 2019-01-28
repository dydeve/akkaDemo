package akkadb.core.db.replica

import akkadb.consistenthashing.NodeId
import akkadb.core.db.actor.StorageNodeActorRef
import akkadb.core.db.cluster.ClusterMembers

/**
  * @Description:
  * @Date 下午6:09 2019/1/28
  * @Author: joker
  */
case class ResolvedNodeAddresses(local: Boolean, remotes: List[StorageNodeActorRef])

object ResolvedNodeAddresses {
  def apply(nodeId: NodeId, preferenceList: PreferenceList, clusterMembers: ClusterMembers): ResolvedNodeAddresses = {
    ResolvedNodeAddresses(
      local = preferenceList.all.contains(nodeId),
      remotes = preferenceList.all.flatMap(clusterMembers.get)
    )
  }
}
