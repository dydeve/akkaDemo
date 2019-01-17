package akkadb.vectorclock

/**
  * thread-safe, no state
  * @Date 上午12:36 2019/1/18
  * @Author: joker
  */
case class Counter(value: Int) extends AnyVal {
  //def addOne: Counter = this.copy(value = value + 1)
  def addOne: Counter = Counter(value + 1)

  def max(other: Counter) = Counter(math.max(`value`, other.value))
}

object Counter {

  def max(c1: Counter, c2: Counter): Counter =
    Counter(math.max(c1.value, c2.value))

  def zero: Counter =
    Counter(0)

}

