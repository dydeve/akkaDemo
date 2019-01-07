package chapter01.akkademy

import akka.actor.{Actor, ActorLogging}

import scala.collection.mutable

class AkkademyDb extends Actor with ActorLogging {

  //val log = Logging(context.system, this)
  val map = mutable.Map.empty[String, Any]


  override def preStart(): Unit = log.info("start")

  override def postStop(): Unit = log.info("stop")

  override def receive: Receive = {
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {} value: {}", key, value)
      //map += (key -> value)
      map.put(key, value)

    case GetRequest(key) =>
      log.info("get key:{} with value:{}", key, map(key))

    case o =>
      log.info("received unknown message: {}", o)
  }
}
