package chapter01.akkademy

import akka.actor.{ActorSystem}
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.duration._

/**
  * TestActorRef 是通用的，有两个功能:
  * 首先，它提供的 Actor API 是同步的,这样我们就不需要在测试中考虑并发的问题;
  * 其次，我们可以通过 TestActorRef 访问其背后的 Actor 对象
  */
class AkkademyDbSpec extends FunSpecLike with Matchers {

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)

  describe("akkademyDb") {

    describe("given SetRequest") {
      it("should place key/value into map") {

        val actorRef = TestActorRef(new AkkademyDb)
        //Actor.noSender  --->  定义该消息并不需要任何响应对象
        actorRef ! SetRequest("key", "value")
        //tell调用请求处理完成后，才会继续处理；没有展示出 Actor API 的异步特性

        val akkademyDb = actorRef.underlyingActor
        akkademyDb.map.get("key") should equal(Some("value"))
      }

    }

    describe("no given SetRequest") {
      it("should no key/value in map") {
        val actorRef = TestActorRef(new AkkademyDb)
        //tell
        actorRef ! SetRequest("key", "value")

        val akkademyDb = actorRef.underlyingActor
        akkademyDb.map.get("key2") should equal(None)
      }
    }

    describe("test get value by key") {
      it("throw exception and restart") {
        val actorRef = TestActorRef(new AkkademyDb)
        //tell
        actorRef ! SetRequest("key", "value")
        actorRef ! "UFO"

        actorRef ! GetRequest("key")

        actorRef ! GetRequest("key1")

        /**
          * [akka://default/user/$$a] start
          * ...
          * java.util.NoSuchElementException: key not found: key1
          * ...
          * [akka://default/user/$$a] stop
          * [akka://default/user/$$a] start
          */
      }
    }
  }

}
