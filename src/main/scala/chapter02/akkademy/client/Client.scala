package chapter02.akkademy.client

import akka.actor.ActorSystem
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.ask
import chapter02.akkademy.messages.{Delete, GetRequest, SetIfNotExists, SetRequest}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

/**
  * [akka.remote.Remoting] Starting remoting
  * [akka.remote.Remoting] Remoting started; listening on addresses :[akka.tcp://localSystem@127.0.0.1:2552]
  * [akka.remote.Remoting] Remoting now listens on addresses: [akka.tcp://localSystem@127.0.0.1:2552]
  */
class Client(remoteAddress: String) {
  private implicit val timeout = Timeout(2 second)
  private implicit val system = ActorSystem("localSystem", ConfigFactory.load("client.conf"))//2552

  //127.0.0.1:2553
  private val remoteDb = system.actorSelection(s"akka.tcp://akkademy@$remoteAddress/user/db")

  def set(key: String, value: String): Future[Any]  = {
    remoteDb ? SetRequest(key, value)//timeout
  }

  def setIfNotExists(key: String, value: String): Future[Any] = {
    remoteDb ? SetIfNotExists(key, value)
  }

  def get(key: String): Future[Any] = {
    remoteDb ? GetRequest(key)//timeout
  }

  def delete(key: String): Future[Any] = {
    remoteDb ? Delete(key)
  }
}
