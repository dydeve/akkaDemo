package chapter04.client

import akka.actor.{ActorSystem, Status}
import akka.testkit.{TestActorRef, TestProbe}
import chapter04.akkademy.{AkkademyDb, SetRequest}
import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

/**
  * @Description:
  * @Date 上午11:15 2019/1/11
  * @Author: joker
  */
class AkkademyDbSpec extends FunSpec with Matchers {
  implicit val system = ActorSystem("system", ConfigFactory.empty()) //ignore config for remoting

  describe("akkademyDb") {
    describe("given SetRequest") {
      val testProbe = TestProbe()

      it("should place key/value into map") {
        val actorRef = TestActorRef(new AkkademyDb)
        actorRef ! SetRequest("key", "value", testProbe.ref)

        val akkademyDb = actorRef.underlyingActor
        akkademyDb.map.get("key") should equal(Some("value"))
      }
    }
  }

  describe("given List[SetRequest]"){
    it("should place key/values into map"){
      val testProbe = TestProbe()

      val actorRef = TestActorRef(new AkkademyDb)
      actorRef ! List(
        SetRequest("key", "value", testProbe.ref),
        SetRequest("key2", "value2", testProbe.ref)
      )

      val akkademyDb = actorRef.underlyingActor
      akkademyDb.map.get("key") should equal(Some("value"))
      akkademyDb.map.get("key2") should equal(Some("value2"))
      testProbe.expectMsg(Status.Success)
      testProbe.expectMsg(Status.Success)
    }
  }
}
