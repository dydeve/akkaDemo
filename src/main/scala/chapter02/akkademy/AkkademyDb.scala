package chapter02.akkademy

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Status}
import chapter02.akkademy.messages._
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.io.StdIn

/**
  * @Description:
  * @Date 下午7:22 2019/1/8
  * @Author: joker
  */
class AkkademyDb extends Actor with ActorLogging {

  val cache = mutable.Map.empty[String, String]

  override def receive: Receive = {
    case SetRequest(key, value) =>
      log.info("receive key:{}, value:{}", key, value)
      cache.put(key, value)
      sender ! Status.Success

    case SetIfNotExists(key, value) =>
      if (cache.contains(key)) {
        log.info("key:{} exists, not set", key)
        sender ! cache.get(key).get
      } else {
        log.info("key:{} not exist, set value:{}", key, value)
        cache.put(key, value)
        sender ! None
      }

    case GetRequest(key) =>
      log.info("get key:{}", key)
      val opt = cache.get(key)
      opt match {
        case Some(x) =>
          sender ! x
        case None =>
          sender ! Status.Failure(new KeyNotFoundException(key))
      }

    case Delete(key) =>
      log.info("delete key:{}", key)
      cache.remove(key)
      sender ! Status.Success

    case o =>
      sender ! Status.Failure(new ClassNotFoundException)

  }
}

/**
  * [akka.remote.Remoting] Starting remoting
  * [akka.remote.Remoting] Remoting started; listening on addresses :[akka.tcp://akkademy@127.0.0.1:2552]
  * [akka.remote.Remoting] Remoting now listens on addresses: [akka.tcp://akkademy@127.0.0.1:2552]
  */
object Main extends App {

  //读取配置文件
  val system = ActorSystem("akkademy", ConfigFactory.load("cache.conf"))//"application.conf" 默认的，会被其他system读走，改名字

  val actor = system.actorOf(Props[AkkademyDb], "db")

  try {
    StdIn.readLine()
  }
  finally {
    system.terminate()
    //TestKitBase#shutdown(system)
  }
}
