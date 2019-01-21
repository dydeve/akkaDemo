package akkadb.vectorclock

import org.scalatest.{FlatSpec, Matchers}

/**
  * @Description:
  * @Date 下午4:19 2019/1/18
  * @Author: joker
  */
class VectorClockOpsSpec extends FlatSpec with Matchers {

  behavior of "vector clock ops"

  it should "create Vector Clock instance from plain string" in {
    "a:2".toVectorClock[String] shouldBe VectorClock(Map("a" -> Counter(2)))
    "a:1, b:2".toVectorClock[String] shouldBe VectorClock(Map("a" -> Counter(1), "b" -> Counter(2)))
  }


  it should "create vector clock instance from plain string with numerical ids" in {
    import VectorClockOps.string2Int
    implicit def string2Int(x: String) = x.toInt
    import VectorClockOps.toVectorClock
    "1:2".toVectorClock[Int] shouldBe VectorClock(Map(1 -> Counter(2)))
    ("1:2": VectorClock[Int]) shouldBe VectorClock(Map(1 -> Counter(2)))
  }

}
