package akkadb.vectorclock

/**
  * thread-safe, val state
  * @Date 上午12:36 2019/1/18
  * @Author: joker
  */
case class Counter(value: Int) extends AnyVal {
  //def addOne: Counter = this.copy(value = value + 1)
  def addOne: Counter = Counter(value + 1)

  //Counter(math.max(`value`, other.value))
  def max(other: Counter): Counter = {
    if (value >= other.value) {
      this
    } else {
      other
    }
  }

}

object Counter {

  def max(c1: Counter, c2: Counter): Counter =
    Counter(math.max(c1.value, c2.value))

  def zero: Counter =
    Counter(0)

}

