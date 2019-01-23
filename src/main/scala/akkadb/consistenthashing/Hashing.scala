package akkadb.consistenthashing

import com.google.common.base.Charsets
import com.google.common.hash.{HashFunction, Hashing}

import scala.annotation.tailrec


/**
  * refer guava Hashing
 *
  * @Date 下午5:39 2019/1/22
  * @Author: joker
  */
object Hash {

  val mod = Math.pow(2.0, 31)
  val INT_MASK = 0xffffffffL

  val murmur3_128 = Hashing.murmur3_128

  def murmur3_128(input: String): Int =
    murmur3_128.hashString(input, Charsets.UTF_8).asInt()

  def murmur3_128Positive(input: String): Int =
    murmur3_128.hashString(input, Charsets.UTF_8).asInt() & Integer.MAX_VALUE


  def consistentHash(input: Int, buckets: Int): Int = {
    require(buckets > 0, s"buckets must be positive: $buckets")
    val generator = LinearCongruentialGenerator(input & INT_MASK)

    // Jump from bucket to bucket until we go out of range
    // 使用尾递归代替while
    @tailrec
    def _jump(candidate: Int): Int = {
      val next = ((candidate + 1) / generator.nextDouble).toInt
      if (next <0 || next >= buckets) {
        candidate
      } else {
        _jump(next)
      }
    }

    _jump(0)
  }

  //当节点失活时，input+1，进入下一轮hash(有次数限制)。亦可启动备用节点
  def consistentHash(input: Int, buckets: Int, reHashNum: Int = 1, checkAlive: Int => Boolean): Int = {
    require(buckets > 0, s"buckets must be positive: $buckets")
    require(reHashNum >= 0, "reHash too many times")
    val generator = LinearCongruentialGenerator(input & INT_MASK)

    // Jump from bucket to bucket until we go out of range
    // 使用尾递归代替while
    @tailrec
    def _jump(candidate: Int): Int = {
      val next = ((candidate + 1) / generator.nextDouble).toInt
      if (next <0 || next >= buckets) {
        candidate
      } else {
        _jump(next)
      }
    }

    val candidate = _jump(0)
    if (checkAlive != null && !checkAlive(candidate)) {
      consistentHash(input + 1, buckets, reHashNum -1, checkAlive)
    }
    candidate
  }
}

case class LinearCongruentialGenerator(seed: Long) {
  private var state = seed

  def nextDouble: Double = {
    state = 2862933555777941757L * state + 1
    return ((state >>> 33).toInt + 1).toDouble / Hash.mod
  }
}