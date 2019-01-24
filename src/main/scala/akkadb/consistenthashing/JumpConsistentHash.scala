package akkadb.consistenthashing

/**
  * jump consistent hash
  * https://segmentfault.com/a/1190000011966218
  *
  * 多种一致性hash的比较 技术博客综合(fb，淘宝，360，netflix)
  * https://colobu.com/2016/03/22/jump-consistent-hash/
  *
  * 当节点down掉，要么启用备份节点，要么rehash(如key + 1)
  * @Date 下午11:35 2019/1/23
  * @Author: joker
  *
  */
@Deprecated
object JumpConsistentHash {

  //jumpHash(key: Long, num_buckets: Int): Long jumpHash(2, 100)=-180
  def jumpHash(key: Long, num_buckets: Int): Long = {
    var k = key
    var buckets = num_buckets
    if (num_buckets <= 0) {
      buckets = 1
    }

    var b = -1L
    var j = 0L
    while (j < buckets) {
      b = j
      k = k * 2862933555777941757L + 1
      // (b + 1) * (double(1LL << 31)/double((key >> 33) + 1))
      j = ((b + 1) * ((1L << 31) * 1.0 / (k >> 33 + 1))).toLong
    }

    b
  }
}
