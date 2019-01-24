package akkadb.consistencehashing

import akkadb.consistenthashing.{Cluster, Hash, Node}
import org.scalatest.{FunSpec, Matchers}

import scala.collection.mutable.ArrayBuffer

/**
  * @Description:
  * @Date 下午11:50 2019/1/24
  * @Author: joker
  */
class ClusterSpec extends FunSpec with Matchers {

  describe("cluster test") {
    var nodes = new ArrayBuffer[Node]()

    nodes += (Node("0"), Node("1"), Node("2"))

    (3 until 100).foreach { i =>
      nodes += Node(i.toString)
    }


    val cluster = Cluster(nodes)
    it("test add node") {
      val nodes1 = new ArrayBuffer[Node](nodes.size + 2)
      nodes.copyToBuffer(nodes1)
      nodes1 += (Node("100"), Node("101"))
      val cluster1 = Cluster(nodes1)

      cluster1.buckets should equal(cluster.buckets + 2)

      var changes = 0
      (1 to 1000000).foreach{ i =>
        val hash = Hash.murmur3_128(s"saaaaaa$i")
        val node = cluster.findNode(hash)
        val node1 = cluster1.findNode(hash)
        if (node.id != node1.id) {
          changes += 1
        }
      }
      print(changes)//19685
    }

    it("test remove and active node") {
      val hash = 10
      val node0 = cluster.findNode(hash)
      cluster.removeNode(node0)
      val node1 = cluster.findNode(hash)
      node0 shouldNot equal(node1)
      cluster.activeNode(node0)

      val node2 = cluster.findNode(hash)
      node0 should equal(node2)

    }
  }

}
