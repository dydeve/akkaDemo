package akkadb.vectorclock

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

/**
  * @Description:
  * @Date 下午3:56 2019/1/18
  * @Author: joker
  */
class VectorClockTest extends FlatSpec with Matchers {

  behavior of "vector clock"

  it should "empty's value equal 0" in {
    val id = UUID.randomUUID()
    val vc = VectorClock.empty(id)
    vc shouldBe VectorClock(Map(id -> Counter(0)))
  }

  it should "increase counter by one" in {
    val id = UUID.randomUUID();
    val vc = VectorClock.empty(id)

    val increased = vc.increase(id)
    increased shouldBe VectorClock(Map(id -> Counter(1)))
  }

  it should "merge two vector clocks" in {
    val id1 = UUID.randomUUID()
    val vc1 = VectorClock(Map(id1 -> Counter(109)))

    val id2 = UUID.randomUUID()
    val vc2 = VectorClock(Map(
      id1 -> Counter(1),
      id2 -> Counter(99)
    ))

    val receiverId = id1

    val merged = VectorClock.merge(receiverId, vc1, vc2)

    merged.get(id1).get shouldBe Counter(109).addOne
    merged.get(id2).get shouldBe Counter(99)
  }

  it should "init an empty vector clock" in {
    type Id = Int
    val vc = VectorClock.apply[Id]()

    vc shouldBe VectorClock(Map.empty[Id, Counter])
  }

}
