package akkadb.vectorclock

import org.scalatest.{FunSpecLike, Matchers}

/**
  * @Description:
  * @Date 下午5:12 2019/1/18
  * @Author: joker
  */
class VectorClockComparatorSpec extends FunSpecLike with Matchers {
  implicit def string2Int(x: String) = x.toInt
  import VectorClockOps.toVectorClock

  val comparator = new VectorClockComparator[String]

  //FunSpecLike
  describe("Consequent") {
    it("1") {
      val vc1 = "a:1"
      val vc2 = "a:1,b:1"
      comparator.apply(vc1, vc2) should equal(Consequent)
    }

    it("2") {
      val vc1 = "a:1,b:1"
      val vc2 = "a:1,b:1"
      comparator.apply(vc1, vc2) should equal(Consequent)
    }

    it("3") {
      val vc1 = "a:1,b:1"
      val vc2 = "a:1,b:2"
      comparator.apply(vc1, vc2) should equal(Consequent)
    }
  }


  describe("Predecessor") {
    it("1") {
      val vc1 = "a:1"
      val vc2 = "a:0"
      comparator.apply(vc1, vc2) should equal(Predecessor)
    }

    it("2") {
      val vc1 = "a:1,b:1"
      val vc2 = "a:1"
      comparator.apply(vc1, vc2) should equal(Predecessor)
    }
  }

  describe("Conflict") {
    it("1") {
      val vc1 = "a:1"
      val vc2 = "b:0"
      comparator.apply(vc1, vc2) should equal(Conflict)
    }

    it("2") {
      val vc1 = "a:0,b:1"
      val vc2 = "a:1,b:0"
      comparator.apply(vc1, vc2) should equal(Conflict)
    }

    it("3") {
      val vc1 = "a:1,b:1,c:1"
      val vc2 = "a:1,b:0,d:1"
      comparator.apply(vc1, vc2) should equal(Conflict)
    }
  }
}
