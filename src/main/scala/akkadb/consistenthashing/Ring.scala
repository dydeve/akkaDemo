package akkadb.consistenthashing


import akkadb.consistenthashing.Ring.RingPartitionId

/**
  * [vnode，realNode]
  * 虚拟节点的数量不会变，所以 一致性hash 比hash好！，因为vnode的数量不变，增减real node时，"稳定"
  * num(vnode) >= num(real node) * n + 1
  * or
  * num(vnode) >= num(maxExceptedRealNode) * n + 1
  *
  * 此算法节点均匀分布，当1节点挂掉，1所有vnode的负载会全部转移到2！
  * @Date 下午3:59 2019/1/21
  * @Author: joker
  */
@Deprecated
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

  //todo removeNode()

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
      /*val takeOverDataFrom: List[Option[(Int, NodeId)]] = (0 until ring.size by (ring.nodesId.size + 1))
        .map {ringPartitionId => //Optional会被 处理 掉
          ring.getNodeId(ringPartitionId).map(nodeId => (ringPartitionId, nodeId))
        }.toList*/

      val updatedRing = takeOverDataFrom.foldLeft(ring) {
        case (acc, (ringPartitionId, _)) => acc.updated(ringPartitionId, nodeId)
      }

      UpdatedRingWithTakenPartitions(updatedRing, takeOverDataFrom)
    }
  }

  //todo def removeNode

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
