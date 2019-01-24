package akkadb.consistenthashing

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * not thread safe, should used in actor
  * @Date 下午10:37 2019/1/24
  * @Author: joker
  */
case class Cluster(private var nodes: ArrayBuffer[Node]) {

  var deadNode = mutable.Set.empty[Node]

  def findNode(hashCode: Int, reHashTimes: Int = 1): Node = {
    val index = Hash.consistentHash(hashCode, nodes.size, reHashTimes, checkAlive)
    nodes(index)
  }


  def buckets: Int = nodes.length

  def addNode(x: Node): Unit= {
    nodes += x
  }

  def removeNode(x: Node): Boolean = {
    deadNode.add(x)
  }


  def activeNode(node: Node): Boolean = {
    deadNode.remove(node)
  }


  def checkAlive(index: Int): Boolean = {
    val node = nodes(index)
    !deadNode.contains(node)
  }
}
