package chapter04.akkademy.client

import akka.actor.SupervisorStrategy.{Restart, Stop, defaultDecider}
import akka.actor.{Actor, ActorIdentity, ActorInitializationException, ActorKilledException, DeathPactException, Identify, OneForOneStrategy, Stash, SupervisorStrategy}
import common.LoggingActor

import scala.concurrent.duration._

/**
  * @Description:
  * @Date 上午11:31 2019/1/11
  * @Author: joker
  */
case class ServerRestarted(path: String)

case class RemoteDieException(s: String) extends Exception

class HeartbeatHotswapClientActor(
                                   address: String,
                                   maxTry: Int = 2) extends LoggingActor with Stash {

  private var tries = 0

  private [this] var remote = context.actorSelection(address)
  implicit val ec = context.dispatcher

  val heartbeatTask = context.system.scheduler.schedule(2 second, 2 second)(
    sendHeartbeat
  )

  def sendHeartbeat(): Unit = {
    remote ! Identify(System.currentTimeMillis())
  }

  override def preStart(): Unit = {
    log.info("preStart")
    sendHeartbeat
  }

  override def postStop(): Unit = {
    log.info("postStop")
    heartbeatTask.cancel()
  }


  /**
    * final val defaultDecider: Decider = {
    *   case _: ActorInitializationException ⇒ Stop
    *   case _: ActorKilledException         ⇒ Stop
    *   case _: DeathPactException           ⇒ Stop
    *   case _: Exception                    ⇒ Restart
    * }
    *
    * @return
    */
  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
    case _: ActorInitializationException ⇒ Stop
    case _: ActorKilledException         ⇒ Stop
    case _: DeathPactException           ⇒ Stop
    case _: RemoteDieException           =>
      log.info("restart for:", _)
      Restart
    case _: Exception                    ⇒ Restart
  }

  override def receive: Receive = {
    case ServerRestarted(path) =>
      remote = context.actorSelection(path)
    case ActorIdentity(correlationId, Some(ref)) =>
      tries = 0
      log.info("remote active, in origin receive, heartbeat:{}, remote:{}", correlationId, ref.path)

      unstashAll()
      context.become(online)
    case ActorIdentity(correlationId, None) =>
      log.info("remote may die, in origin receive, heartbeat:{}", correlationId)
      tries += 1
      if (tries >= maxTry) {
        //重启Actor 防止stash爆满
        throw new RemoteDieException(s"has tried:$tries times, restart now")
      }

    case _ =>
      stash()
  }

  def online: Receive = {
    case ActorIdentity(correlationId, Some(ref)) =>
      tries = 0
      log.info("remote active, in online receive, heartbeat:{}, remote:{}", correlationId, ref.path)
    case ActorIdentity(correlationId, None) =>
      tries += 1
      /*if (tries >= maxTry) {
        //重启Actor
        throw new Exception(s"has tried:$tries times, restart now")
      }*/
      log.info("remote may die, in origin receive, heartbeat:{}", correlationId)
      context.unbecome()
    case x =>
      remote forward x

  }
}
