package akkadb.vectorclock

import akka.cluster.protobuf.msg.ClusterMessages

/**
  * akka.cluster.protobuf.msg.ClusterMessages.VectorClock
  *
  * @Date 上午12:49 2019/1/18
  * @Author: joker
  */
case class VectorClock[Id](private val clock: Map[Id, Counter]) {

  def get(id: Id): Option[Counter] = clock.get(id)

  def increase(id: Id): VectorClock[Id] = {
    val currentClock = clock.getOrElse(id, Counter.zero)
    val increasedClock = currentClock.addOne
    VectorClock(clock + (id -> increasedClock))
  }

  def toList: List[(Id, Counter)] = clock.toList

  def keys: Set[Id] = clock.keySet
}

object VectorClock {

  def apply[Id](): VectorClock[Id] = VectorClock(Map.empty[Id, Counter])

  def empty[Id](id: Id): VectorClock[Id] = VectorClock(Map(id -> Counter.zero))

  def merge[Id](receiverId: Id, vc1: VectorClock[Id], vc2: VectorClock[Id]): VectorClock[Id] = {
    val mergedClock = vc1.clock ++ vc2.clock

    val mergedCounter: Counter = (vc1.get(receiverId), vc2.get(receiverId)) match {
      case (Some(counter1), Some(counter2)) => counter1.max(counter2)
      case (Some(counter1), None) => counter1
      case (None, Some(counter2)) => counter2
      case (None, None) => Counter.zero
    }

    VectorClock(mergedClock + (receiverId -> mergedCounter.addOne))//notice addOne here
  }

}
