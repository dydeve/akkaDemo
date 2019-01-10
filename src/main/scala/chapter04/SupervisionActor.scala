package chapter04

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, SupervisorStrategy}

/**
  * @Description:
  * @Date 下午4:20 2019/1/10
  * @Author: joker
  */

object BrokenPlateException extends Exception

object DrunkenFoolException extends Exception

object RestaurantFireError extends Exception

object TiredChefException extends Exception

class SupervisionActor extends Actor with ActorLogging {


  /**
    * final val defaultDecider: Decider = {
    * case _: ActorInitializationException ⇒ Stop
    * case _: ActorKilledException         ⇒ Stop
    * case _: DeathPactException           ⇒ Stop
    * case _: Exception                    ⇒ Restart
    * }
    *
    * @return
    */
  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy() {
      case BrokenPlateException => Resume
      case DrunkenFoolException => Restart
      case RestaurantFireError => Escalate
      case TiredChefException => Stop
      case _ => Escalate
    }
  }

  override def receive: Receive = ???
}
