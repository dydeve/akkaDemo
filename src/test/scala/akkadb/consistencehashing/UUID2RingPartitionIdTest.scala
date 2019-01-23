package akkadb.consistencehashing

import java.util.UUID

import akkadb.consistenthashing.{Ring, UUID2RingPartitionId}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @Description:
  * @Date 下午11:55 2019/1/21
  * @Author: joker
  */
class UUID2RingPartitionIdTest extends FlatSpec with Matchers {

  behavior of "behavior of map function from uuid to ring's partitionId"

  it should "use hashcode" in {
    val uuid = UUID.randomUUID();
    val ring = Ring(partitionSize = 2, nodeSize = 3)
    val computedVal = UUID2RingPartitionId.apply(uuid, ring)
    val expectedVal = uuid.hashCode() & Integer.MAX_VALUE % ring.size
    computedVal should equal(expectedVal)
  }

}
