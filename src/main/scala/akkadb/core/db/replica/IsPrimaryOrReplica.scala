package akkadb.core.db.replica

import java.util.UUID

import akkadb.consistenthashing.{NodeId, Ring, UUID2RingPartitionId}
import akkadb.storage.api.PluggableStorageProtocol.DataOriginality

/**
  * @Description:
  * @Date 下午5:32 2019/1/28
  * @Author: joker
  */
class IsPrimaryOrReplica(nodeId: NodeId, ring: Ring) extends (UUID => DataOriginality) {

  override def apply(id: UUID): DataOriginality = {
    val partitionId = UUID2RingPartitionId(id, ring)

    if (ring.getNodeId(partitionId).contains(nodeId)) {
      DataOriginality.Primary(partitionId)
    } else {
      DataOriginality.Replica(partitionId)
    }
  }
}
