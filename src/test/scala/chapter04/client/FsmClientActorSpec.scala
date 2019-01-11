package chapter04.client

import akka.actor.{ActorSystem, Props, Status}
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import chapter04.akkademy.{AkkademyDb, SetRequest}
import chapter04.akkademy.client._
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._

/**
  * @Description:
  * @Date 上午12:15 2019/1/11
  * @Author: joker
  */
class FsmClientActorSpec extends FunSpec with Matchers {

  implicit val system = ActorSystem("test-system", ConfigFactory.defaultReference())
  implicit val timeout = Timeout(5 second)

  val dbRef = TestActorRef[AkkademyDb](Props[AkkademyDb], "db")
  val db = dbRef.underlyingActor
  val testProbe = TestProbe()

  describe("FsmClientActor") {
    val fsmClientRef = TestActorRef[FSMClientActor](Props(classOf[FSMClientActor], dbRef.path.toString), "fsm")
    val fsmClient = fsmClientRef.underlyingActor

    it("should transition from DisConnected to ConnectedAndPending when getting a msg") {
      //fsmClient.stateName should equal(DisConnected)
      fsmClientRef ! SetRequest("key", "value", testProbe.ref)
      fsmClient.stateName should equal(ConnectedAndPending)
      db.map.get("key") should equal(None)
    }

    it("should transition from ConnectedAndPending to Connected when flushing") {
      fsmClientRef ! SetRequest("key", "value", testProbe.ref)
      fsmClient.stateName should equal(ConnectedAndPending)
      fsmClientRef ! Flush
      fsmClient.stateName should equal(Connected)
      db.map.get("key") should equal(Some("value"))
      testProbe.expectMsg(Status.Success)
    }

    it("should transition from Disconnected to ConnectedAndPending when getting a msg") {
      //fsmClient.stateName should equal(DisConnected)
      fsmClientRef ! SetRequest("key2", "value", testProbe.ref)
      fsmClient.stateName should equal(ConnectedAndPending)
      db.map.get("key2") should equal(None)

    }
  }

}
