package akkadb.consistencehashing

import akkadb.consistenthashing.Hash
import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.scalatest.{FunSpec, Matchers}

/**
  * @Description:
  * @Date 下午10:33 2019/1/22
  * @Author: joker
  */
class HashSpec extends FunSpec with Matchers {

  describe("test node in consistent hash") {
    it("show server") {

    }
  }

  describe("test hash") {
    it("guava consistent hash write in scala VS guava java") {
      val buckets = 1024
      var same = 0
      var different = 0
      (0 to 10000000).foreach { i =>
        val hash = Hash.murmur3_128(s"hash#$i")
        val scalaResult = Hash.consistentHash(hash, buckets)
        val guavaResult = Hashing.consistentHash(hash & 0xffffffffL, buckets)
        if (scalaResult == guavaResult) {
          same += 1
        } else {
          different += 1
        }
      }
      same shouldBe 10000001
      different shouldBe 0
    }

    it("test scala consistent hash when node change") {
      var same = 0
      var different = 0
      (0 until 1000000).foreach{i =>
        val hash = Hash.murmur3_128(s"node#$i")
        if (Hash.consistentHash(hash, 1024) == Hash.consistentHash(hash, 1048)) {
          same += 1
        } else {
          different += 1
        }
      }
      println(s"same: $same, different: $different. rate = ${same.toDouble/(same + different)}")
    }

    it("guava consistent hash when node change") {
      val nodeNum = 999

      var same = 0
      var change = 0
      (0 to 100000).foreach { i =>
        val x = Hashing.murmur3_32.hashString("test" + i, Charsets.UTF_8).asInt

        /**
          * When you reduce the number of buckets, you can accept that the most recently added
          * buckets will be removed first. More concretely, if you are dividing traffic among tasks,
          * you can decrease the number of tasks from 15 and 10, killing off the final 5 tasks, and
          * {@code consistentHash} will handle it. If, however, you are dividing traffic among
          * servers {@code alpha}, {@code bravo}, and {@code charlie} and you occasionally need to
          * take each of the servers offline, {@code consistentHash} will be a poor fit: It provides
          * no way for you to specify which of the three buckets is disappearing. Thus, if your
          * buckets change from {@code [alpha, bravo, charlie]} to {@code [bravo, charlie]}, it will
          * assign all the old {@code alpha} traffic to {@code bravo} and all the old {@code bravo}
          * traffic to {@code charlie}, rather than letting {@code bravo} keep its traffic.
          */
        if (Hashing.consistentHash(x, nodeNum) == Hashing.consistentHash(x, nodeNum + 1)) same += 1
        else change += 1
        //println(Hashing.consistentHash(x, nodeNum))
      }

      /**
        * 1
        * 99913
        * 88
        * 8.799912000879991E-4
        *
        * 100
        * 91024
        * 8977
        * 0.08976910230897692
        */
      System.out.println(same)
      System.out.println(change)
      System.out.println(1.0 * change / (same + change))
    }

  }


}
