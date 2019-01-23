package akkadb.consistenthashing

import java.util.UUID

import akkadb.consistenthashing.Ring.RingPartitionId

/**
  * @Description:
  * @Date 下午8:04 2019/1/21
  * @Author: joker
  */
object UUID2RingPartitionId extends ((UUID, Ring) => Ring.RingPartitionId) {

  override def apply(id: UUID, ring: Ring): RingPartitionId = {
    //(id.hashCode() & Integer.MAX_VALUE) % ring.size
    Hash.murmur3_128Positive(id.toString) % ring.size
  }

}
