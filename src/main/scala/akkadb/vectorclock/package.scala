package akkadb

/**
 * @Description: 
 * 
 * @Date 上午1:52 2019/1/18
 * @Author: joker
 */
package object vectorclock {

  /**
    * "A:1,B:1,C:1" => A -> 1, B -> 1, C -> 1
    * @param plain
    */
  implicit class VectorClockOps(plain: String) {

    def toVectorClock[Id](string2Id: String => Id): VectorClock[Id] = VectorClock {
      plain.split(",").map { s: String =>
        val Array(key, value) = s.split(":")
        (string2Id(key), Counter(value.toInt))
      }.toMap
    }

  }

  object VectorClockOps {

    //implicit def stringAsId(s: String): VectorClock[String] = s.toVectorClock[String]
    implicit def stringAsId(s: String): VectorClock[String] = s.toVectorClock(_.asInstanceOf)

    implicit def intAsId(s: String): VectorClock[Int] = s.toVectorClock(_.toInt)

  }

}
