package akkadb.consistenthashing


import akkadb.consistenthashing.Ring.RingPartitionId

/**
  * @Description:
  * @Date 下午3:59 2019/1/21
  * @Author: joker
  */
class Ring(ring: Map[RingPartitionId, NodeId]) {

  lazy val size: Int = ring.size
  lazy val nodesId: Set[NodeId] = ring.values.toSet
  lazy val swap: Map[NodeId, List[RingPartitionId]] =
    ring.groupBy(kv => kv._2).mapValues(_.keys.toList.sorted)
  //val s = ring.map(kv => (kv._2, kv._1))//后面的key会覆盖前面的key

  def getNodeId(id: RingPartitionId): Option[NodeId] = ring.get(id)

  def updated(ringPartitionId: RingPartitionId, nodeId: NodeId): Ring =
    new Ring(ring.updated(ringPartitionId, nodeId)) //new Ring(ring + (ringPartitionId -> nodeId))

  def nextPartitionId(id: RingPartitionId): RingPartitionId = (id + 1) % size

  override def toString: String = ring.toString()
}

object Ring {
  type RingPartitionId = Int

  sealed trait AddNodeResult

  case object AlreadyExistsNodeId extends AddNodeResult

  case class UpdatedRingWithTakenPartitions(ring: Ring, takeOverDataFrom: List[(RingPartitionId, NodeId)])
    extends AddNodeResult

  def addNode(ring: Ring, nodeId: NodeId): AddNodeResult = {
    if (ring.nodesId.contains(nodeId)) {
      AlreadyExistsNodeId
    } else {

      /*val chars = 'a' to 'z'
      val perms = chars flatMap { a =>
        chars flatMap { b =>
          if (a != b) Seq("%c%c".format(a, b))
          else Seq()
        }
      }*/

      // this could be improved e.g. we should rely on least taken resources
      val takeOverDataFrom = (0 until ring.size by (ring.nodesId.size + 1))
        .flatMap { ringPartitionId => //Optional会被 处理 掉
          ring.getNodeId(ringPartitionId).map(nodeId => (ringPartitionId, nodeId))
        }.toList

      val updatedRing = takeOverDataFrom.foldLeft(ring) {
        case (acc, (ringPartitionId, _)) => acc.updated(ringPartitionId, nodeId)
      }

      UpdatedRingWithTakenPartitions(updatedRing, takeOverDataFrom)
    }
  }

  def apply(partitionSize: Int, nodeSize: Int): Ring = {
    val partitions2Nodes = for {
      id <- 0 until nodeSize
      partitionId <- id until partitionSize by nodeSize
    } yield (partitionId, NodeId(id))

    new Ring(partitions2Nodes.toMap)
  }

  def a(partitionSize: Int, nodeSize:Int) = {
    for {
      id <- 0 until nodeSize
      partitionId <- id until partitionSize by nodeSize
    } yield (partitionId, id)

  }

}
