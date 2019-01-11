package chapter04.client

import akka.actor.{ActorSystem, Props, Status}
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import chapter04.akkademy.{AkkademyDb, GetRequest, SetRequest}
import chapter04.akkademy.client.HotswapClientActor
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._

/**
  * @Description:
  * @Date 上午10:22 2019/1/11
  * @Author: joker
  */
class HotswapClientActorSpec extends FunSpec with Matchers {

  implicit val system = ActorSystem("test-system", ConfigFactory.defaultReference())
  implicit val timeout = Timeout(5 seconds)

  describe("hotSwapClientActor") {
    it("should set values") {
      val dbRef = TestActorRef[AkkademyDb]
      val db = dbRef.underlyingActor
      val probe = TestProbe()
      val clientRef = system.actorOf(Props(classOf[HotswapClientActor], dbRef.path.toString))

      clientRef ! new SetRequest("testkey", "testvalue", probe.ref)
      probe.expectMsg(Status.Success)
      db.map.get("testkey") should equal(Some("testvalue"))
    }

    it("should get values") {
      val dbRef = TestActorRef[AkkademyDb]
      val db = dbRef.underlyingActor
      db.map.put("testkey", "testvalue")

      val probe = TestProbe()
      val clientRef = TestActorRef(Props(classOf[HotswapClientActor], dbRef.path.toString))

      clientRef ! new GetRequest("testkey", probe.ref)
      probe.expectMsg("testvalue")
    }
  }

}
