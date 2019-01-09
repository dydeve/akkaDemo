package chapter02

import akka.actor.{ActorIdentity, ActorNotFound, ActorRef, ActorSystem, Identify, Props}
import akka.dispatch.ExecutionContexts
import chapter02.akkademy.ReverseString
import org.scalatest.{FunSpec, Matchers}
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * @Description:
  * @Date 上午1:43 2019/1/9
  * @Author: joker
  */
class ReverseStringSpec extends FunSpec with Matchers {

  private implicit val system = ActorSystem("reverse")
  private implicit val timeout = Timeout(3 second)
  //akka://reverse/user/test
  private val reverseActor = system.actorOf(Props[ReverseString], "test")

  import scala.concurrent.ExecutionContext.Implicits.global

  describe("reverse string") {
    it("should reverse it") {
      val future = reverseActor ? "make progress"
      //Await.result(future, 1 second) should equal("make progress".reverse)
      future onComplete {
        case body => body.get should equal("make progress".reverse)
      }
    }

    /**
      * ActorSelection
        def resolveOne()(implicit timeout: Timeout): Future[ActorRef] = {
          implicit val ec = ExecutionContexts.sameThreadExecutionContext
          val p = Promise[ActorRef]()
          this.ask(Identify(None)) onComplete {
            case Success(ActorIdentity(_, Some(ref))) ⇒ p.success(ref)
            case _                                    ⇒ p.failure(ActorNotFound(this))
          }
          p.future
        }
    */
    it("int should error") {
      //val actorRef = TestActorRef(new ReverseString)
      val future = reverseActor ? 123
      future onComplete {
        case Success(value) => value should equal("fedcba")
        case Failure(exception) => exception.isInstanceOf[IllegalArgumentException]
      }
    }

    it("Future Sequence") {
      val requestList = List("123", "456", "789", 123)
      val expectRespondList = List("123".reverse, "456".reverse, "789".reverse, "sorry")
      //val futurelist:List[Any => Future[Any]] = requestList.map(_ => reverseActor ? _)
      val futurelist:List[Future[Any]] = requestList.map(x => reverseActor ? x)
      /*val responseList: Future[List[String]] =
        Future.sequence(futurelist).mapTo[String]
          .recover({
            case e: Exception => "sorry"
          })*/
      val respondFuture: Future[List[String]] = Future.sequence(
        futurelist.map(future => future.mapTo[String].recover{case e: Exception => "sorry"})
      )

      Await.result(respondFuture, 1 second) should equal(expectRespondList)


    }
  }

}
