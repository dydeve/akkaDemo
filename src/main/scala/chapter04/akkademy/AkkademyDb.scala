package chapter04.akkademy

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Status}

import scala.collection.mutable

/**
  * @Description:
  * @Date 上午1:07 2019/1/11
  * @Author: joker
  */
object Main extends App {
  val system = ActorSystem("akkademy")
  val helloActor = system.actorOf(Props[AkkademyDb], name = "akkademy-db")
}

class AkkademyDb extends Actor with ActorLogging {

  val map = mutable.Map.empty[String, Object]

  override def receive: Receive = {
    /*case "Ping" =>
      sender() ! "Pong"*/
    case x: Connected =>
      sender() ! x
    case x: List[_] =>
      x foreach {
        case SetRequest(key, value, senderRef) =>
          handleSetRequest(key, value, senderRef)
        case GetRequest(key, senderRef) =>
          handleGetRequest(key, senderRef)
      }
    case SetRequest(key, value, senderRef) =>
      handleSetRequest(key, value, senderRef)
    case GetRequest(key, senderRef) =>
      handleGetRequest(key, senderRef)
    case o =>
      log.info("unknown message")
      sender() ! Status.Failure(new ClassNotFoundException)
  }

  def handleSetRequest(key: String, value: Object, senderRef: ActorRef): Unit = {
    log.info("received SetRequest - key: {} value: {}", key, value)
    map.put(key, value)
    senderRef ! Status.Success
  }

  def handleGetRequest(key: String, senderRef: ActorRef): Unit = {
    log.info("received GetRequest - key: {}", key)
    val response: Option[Object] = map.get(key)
    response match {
      case Some(x) => senderRef ! x
      case None => senderRef ! Status.Failure(new KeyNotFoundException(key))
    }
  }

}
