package chapter02.example

import akka.actor.{Actor, Props, Status}

/**
  * @Description:
  * @Date 上午1:57 2019/1/8
  * @Author: joker
  */
object ScalaPongActor {
  def props = Props(classOf[ScalaPongActor])
}

class ScalaPongActor extends Actor {

  /**
    * 将actor封装起来，提供引用，向Actor发送消息并接收响应
    * val actor = system.actorOf(Props(classOf[ScalaPongActor]))
    * system.actorOf(Props(new ScalaPongActor))
    */
  override def receive: Receive = {
    case "ping" =>
      //just return unit
      sender() ! "pong"//sender:  implicit final val self = context.self

    case _ =>
      /**
        * Actor 本身在任何情况下都不会自己返回 Failure(即使 Actor 本身出现错误)。
        * 因此如果想要将发生的错误通知消息发送者，那么我们必 须要主动发送一个 Failure 给对方。
        * 发送回 Failure 会导致请求方的 Future 被标记 为失败。
        */
      sender() ! Status.Failure(new Exception("unknown message"))
  }

}
