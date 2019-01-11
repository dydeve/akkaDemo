package chapter04.client

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{TestActorRef, TestProbe}
import chapter04.akkademy.client.{HeartbeatHotswapClientActor, ServerRestarted}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._

/**
  * @Description:
  * @Date 下午2:26 2019/1/11
  * @Author: joker
  */
class HeartbeatActorSpec extends FunSpec with Matchers {

  implicit val system = ActorSystem("heartbeat")
  val timeout = Timeout(5 second)



  describe("heartbeat hot swap") {
    //val hotSwap = TestActorRef(Props(classOf[HeartbeatHotswapClientActor], server.ref.path.toString))

    it("normal scenes") {
      val server = TestProbe("server-just-reply")
      val client = TestProbe("client-just-request")
      val hotSwap = system.actorOf(Props(classOf[HeartbeatHotswapClientActor], server.ref.path.toString, 2))

      hotSwap.tell("i am a client", client.ref)
      server.expectMsg("i am a client")
      server.reply("ok, i know")
      client.expectMsg("ok, i know")

      Thread.sleep(20000)
    }

    it("server stop and start") {
      var server = TestProbe("server-just-reply")
      val client = TestProbe("client-just-request")



      val hotSwap = system.actorOf(Props(classOf[HeartbeatHotswapClientActor], server.ref.path.toString, 2))
      Thread.sleep(5000)
      server.ref ! PoisonPill

      Thread.sleep(10000)
      hotSwap.tell("i am a client", client.ref)
      hotSwap.tell("i am two client", client.ref)
      hotSwap.tell("i am three client", client.ref)

      server = TestProbe("server-just-reply")
      hotSwap ! ServerRestarted(server.ref.path.toString)
      Thread.sleep(10000)

      val receive = server.receiveN(3, 5 second)
      receive.foreach(println)

      //Thread.sleep(20000)

    }


  }
}
