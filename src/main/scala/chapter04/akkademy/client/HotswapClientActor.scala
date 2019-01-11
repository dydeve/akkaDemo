package chapter04.akkademy.client

import akka.actor.{Actor, ActorIdentity, ActorLogging, Cancellable, Identify, Stash}
import chapter04.akkademy.{Connected, Request}

import scala.concurrent.duration._

/**
  * @Description:
  * @Date 上午1:17 2019/1/11
  * @Author: joker
  */
class HotswapClientActor(address: String) extends Actor with ActorLogging with Stash {

  private val remoteDb = context.system.actorSelection(address)
  private implicit val a = context.dispatcher

  //不可换行
  private val isAvailable: Cancellable = context.system.scheduler.schedule(2 second, 2 second)(remoteDb ! Identify(System.currentTimeMillis()))


  override def preStart(): Unit = {
    //case
  }


  override def postStop(): Unit = {
    isAvailable.cancel()
  }

  //离线处理
  override def receive = {
    case ActorIdentity(correlationId, Some(ref)) =>

    case ActorIdentity(correlationId, None) =>

    case x: Request => //can't handle until we know remote system is responding
      remoteDb ! new Connected //see if the remote actor is up
      stash() //stash message for later
    case _: Connected => // Okay to start processing messages.
      //收到了响应,就说明连接上了
      unstashAll()
      context.become(online)
  }

  //在线处理
  def online: Receive = {
    //收到下线指令
    case x: Disconnected =>
      context.unbecome()
    case x: Request =>
      //本地转发
      remoteDb forward x //forward is used to preserve sender
    //todo 直接tell应该也行 因为sender作为参数传过去了
    //remoteDb forward x
  }
}

/**
  * Disconnect msg is unimplemented in this example.
  * Because we're not dealing w/ sockets directly,
  * we could instead implement an occasional ping/heartbeat that restarts
  * this Actor if the remote actor isn't responding.
  * After restarting, the actor will revert to the
  * default state and stash messages
  */
class Disconnected
