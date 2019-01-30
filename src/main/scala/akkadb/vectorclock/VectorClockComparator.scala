package akkadb.vectorclock

/**
  * @Description:
  * @Date 下午1:56 2019/1/18
  * @Author: joker
  */
class VectorClockComparator[Id] extends ((VectorClock[Id], VectorClock[Id]) => VectorClockRelation) {
  //Function2

  override def apply(vc1: VectorClock[Id], vc2: VectorClock[Id]): VectorClockRelation = {

    import scala.util.control.Breaks._

    def compare(keysOfVc: Set[Id], vc: VectorClock[Id], anotherVc: VectorClock[Id]): Boolean =
      keysOfVc.forall(id => anotherVc.get(id).get.value >= vc.get(id).get.value)


    val vc1Keys = vc1.keys
    val vc2Keys = vc2.keys

    val vc2ContainsAllKeysOfVc1 = vc1Keys.forall(vc2Keys.contains)
    val vc1ContainsAllKeysOfVc2 = vc2Keys.forall(vc1Keys.contains)

    if (vc2ContainsAllKeysOfVc1) {
      if (vc1ContainsAllKeysOfVc2) {
        val (count1, count2) = vc1Keys.foldLeft((0, 0)) { (count, key) =>
          val v1 = vc1.get(key).get.value
          val v2 = vc2.get(key).get.value
          if (v2 == v1) {
            count
          } else if (v2 > v1) {
            (count._1, count._2 + 1)
          } else {
            (count._1 + 1, count._2)
          }
        }

        if (count1 == 0) {
          VectorClockRelation.Consequent
        } else if (count2 == 0) {
          VectorClockRelation.Predecessor
        } else {
          VectorClockRelation.Conflict
        }
      } else {
        //vc2 > vc1
        if (compare(vc1Keys, vc1, vc2)) {
          VectorClockRelation.Consequent
        } else {
          VectorClockRelation.Conflict
        }
      }
    } else {
      if (vc1ContainsAllKeysOfVc2) {
        if (compare(vc2Keys, vc2, vc1)) {
          VectorClockRelation.Predecessor
        } else {
          VectorClockRelation.Conflict
        }
      } else {
        VectorClockRelation.Conflict
      }
    }


  }
}

sealed trait VectorClockRelation

object VectorClockRelation {
  case object Predecessor extends VectorClockRelation

  case object Conflict extends VectorClockRelation

  case object Consequent extends VectorClockRelation
}

