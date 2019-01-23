package akkadb.consistencehashing

import akkadb.consistenthashing.Ring.AlreadyExistsNodeId
import akkadb.consistenthashing.{NodeId, Ring}
import org.scalatest.{FlatSpec, FunSpec, Matchers}

/**
  * @Description:
  * @Date 上午12:03 2019/1/22
  * @Author: joker
  */
class RingSpec extends FunSpec with Matchers {

  describe("test ring") {

    it("size depends on partition size") {
      val partitionSize = 10
      val nodeSize = 11
      val ring = Ring(partitionSize, nodeSize)
      ring.size should equal(partitionSize)
    }

    it("should had expected nodeIds") {
      val ring = Ring(4, 3)
      ring.nodesId should equal(Set(NodeId(0),NodeId(1),NodeId(2)))
    }

    it("initialize Ring with vnodes") {
      val ring = Ring.apply(64, 5)

      val expectedSwappedRing = Map(
        NodeId(0) -> List(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60),
        NodeId(1) -> List(1, 6, 11, 16, 21, 26, 31, 36, 41, 46, 51, 56, 61),
        NodeId(2) -> List(2, 7, 12, 17, 22, 27, 32, 37, 42, 47, 52, 57, 62),
        NodeId(3) -> List(3, 8, 13, 18, 23, 28, 33, 38, 43, 48, 53, 58, 63),
        NodeId(4) -> List(4, 9, 14, 19, 24, 29, 34, 39, 44, 49, 54, 59)
      )

      ring.swap shouldBe expectedSwappedRing
    }

    it("should start from index of 0") {
      val ring = Ring.apply(64, 5)
      ring.getNodeId(0) shouldBe defined
    }

    it("should end index br minus one of ring's size") {
      val ring = Ring.apply(64, 5)
      ring.getNodeId(63) shouldBe defined
      ring.getNodeId(64) should not be defined
    }

    it("should update value for particular key") {
      val ring = Ring.apply(64, 5)
      val ring1 = ring.updated(2, NodeId(100))
      ring.getNodeId(2).get should equal(NodeId(2))
      ring1.getNodeId(2).get should equal(NodeId(100))
    }

  }

  describe("test ring add") {

    it("should meet AlreadyExistsNodeId") {
      val ring = Ring.apply(64, 5)
      Ring.addNode(ring, NodeId(0)) shouldBe AlreadyExistsNodeId
      Ring.addNode(ring, NodeId(1)) shouldBe AlreadyExistsNodeId
      Ring.addNode(ring, NodeId(2)) shouldBe AlreadyExistsNodeId
      Ring.addNode(ring, NodeId(3)) shouldBe AlreadyExistsNodeId
      Ring.addNode(ring, NodeId(4)) shouldBe AlreadyExistsNodeId
    }

    it("take over some partitions by added node") {
      // given
      val nodesSize      = 4
      val partitionsSize = 36
      val ring           = Ring.apply(partitionsSize, nodesSize)

      // when
      val nodeId          = NodeId(5)
      val updateResult    = Ring.addNode(ring, nodeId).asInstanceOf[Ring.UpdatedRingWithTakenPartitions]
      val updatedRing     = updateResult.ring
      val takenPartitions = updateResult.takeOverDataFrom

      // then
      updatedRing.size shouldBe ring.size
      updatedRing.nodesId shouldBe (ring.nodesId + nodeId)

      takenPartitions should not be empty

      println(ring.swap)
      println(updatedRing.swap)
    }

  }

}
