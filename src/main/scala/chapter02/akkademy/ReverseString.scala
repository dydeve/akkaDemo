package chapter02.akkademy

import akka.actor.{Actor, ActorLogging, Status}

/**
  * @Description:
  * @Date 下午7:22 2019/1/8
  * @Author: joker
  */
class ReverseString extends Actor with ActorLogging {

  override def receive: Receive = {
    case string: String =>
      log.info("receive:{} to reverse", string)
      sender ! string.reverse
    case o =>
      log.error("unsupported value:{}", o)
      sender ! Status.Failure(new IllegalArgumentException)
  }
}
