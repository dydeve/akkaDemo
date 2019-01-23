package akkadb.consistenthashing

import java.util

/**
  * 以node的hash作为treeMap的key，难以避免hash碰撞。其他map又不能满足需求(ceilingEntry)
  * 故弃之
  * @Date 下午11:13 2019/1/22
  * @Author: joker
  */
@Deprecated
class ConsistentHashBaseOnTreeMap(nodes: Seq[Node], replicate: Int = 0) {

  private val tree = new util.TreeMap[Int, Node]

  if (replicate == 0) {
    nodes.foreach { node =>
      tree.put(murmur3_128(node.x), node)
    }
  } else {
    nodes.foreach { node =>
      (0 until replicate).foreach{ i =>
        tree.put(murmur3_128(s"node.x#$i"), node)
      }
    }
  }



  implicit def nodeHash(node: Node): Int = murmur3_128(node.x)
  implicit def stringHash(x: String): Int = murmur3_128(x)

  def selectNode(x: String): Node = {
    val entry = tree.ceilingEntry(x)
    if (entry == null) {
      nodes(0)
    } else {
      entry.getValue
    }
  }

  /**
    * 会有hash冲突
    * def addNode(node: Node): Unit = {
    *     tree.put(murmur3_128(node.x), node)
    * }
    */


  def removeNode(node: Node): Unit = {
    tree.remove(node)
  }

  //不同的x会导致相同的hash值(hash冲突), treeMap的key会被覆盖
  @Deprecated
  private def murmur3_128(x: String): Int = {
    Hash.murmur3_128Positive(x)
  }

}

object ConsistentHashBaseOnTreeMap {

}
