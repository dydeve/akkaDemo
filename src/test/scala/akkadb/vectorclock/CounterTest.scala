package akkadb.vectorclock

import org.scalatest.{FlatSpec, Matchers}

/**
  * @Description:
  * @Date 下午3:44 2019/1/18
  * @Author: joker
  */
class CounterTest extends FlatSpec with Matchers {

  behavior of "Counter"

  it should "create new Counter when call addOne" in {
    val counter = Counter(0)
    val increased = counter.addOne

    increased should equal(Counter(1))
    increased shouldBe (Counter(1))
  }

  it should "choose max" in {
    val counter1 = Counter(1)
    val counter2 = Counter(2)
    counter1.max(counter2) shouldBe(counter2)
  }
}
