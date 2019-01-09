package chapter02

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import chapter02.example.ScalaPongActor
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future, Promise}


/**
  * @Description:
  * @Date 下午3:23 2019/1/8
  * @Author: joker
  */
class ScalaAskExamplesTest extends FunSpecLike with Matchers {

  val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  val pongActor = system.actorOf(ScalaPongActor.props) //system.actorOf(Props(classOf[ScalaPongActor]))

  describe("Pong actor") {

    it("should respond with Pong") {
      val future = pongActor ? "ping"
      //uses the implicit timeout
      val result = Await.result(future.mapTo[String], 1 second) //notice: 不要在非测试代码中休眠或阻塞线程
      assert(result == "pong")
    }

    it("should fail on unknown message") {
      val future = pongActor ? "no"
      intercept[Exception] {
        val result = Await.result(future.mapTo[String], 1 second)
      }

      /**
        * val result = Await.result(future.mapTo[String], 1 second)
        *
        * unknown message
        * java.lang.Exception: unknown message
        * at chapter02.example.ScalaPongActor$$anonfun$receive$1.applyOrElse(ScalaPongActor.scala:32)
        * at akka.actor.Actor.aroundReceive(Actor.scala:517)
        * at akka.actor.Actor.aroundReceive$(Actor.scala:515)
        * at chapter02.example.ScalaPongActor.aroundReceive(ScalaPongActor.scala:14)
        * at akka.actor.ActorCell.receiveMessage(ActorCell.scala:588)
        * at akka.actor.ActorCell.invoke(ActorCell.scala:557)
        * at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:258)
        * at akka.dispatch.Mailbox.run(Mailbox.scala:225)
        * at akka.dispatch.Mailbox.exec(Mailbox.scala:235)
        * at akka.dispatch.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
        * at akka.dispatch.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
        * at akka.dispatch.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
        * at akka.dispatch.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
        */
    }
  }

  def askPong(message: String): Future[String] = (pongActor ? message).mapTo[String]

  describe("FutureExamples") {
    import scala.concurrent.ExecutionContext.Implicits.global

    it("should print to console") {
      askPong("ping").onSuccess({
        case x: String => println(s"replies with:$x")
      })
      Thread.sleep(100)
    }

    it("should transform async") {
      val f: Future[String] = askPong("Ping").flatMap(x => {
        assert(x == "Pong")
        askPong("Ping")
      })
      val c = Await.result(f, 1 second)
      c should equal("Pong")

      //val listOfFutures: List[Future[String]] = List("Pong", "Pong", "failed").map(x => askPong(x))
    }

    it("should effect on failure") {
      askPong("causeError").onFailure {
        case e: Exception => println(s"Got exception:$e")
      }
    }

    it("recover from failure") {
      val f = askPong("causeError").recover {
        case t: Exception => "default"
      }
      "default" should equal(Await.result(f.mapTo[String], 1 second))
    }

    it("recover from failure async") {
      askPong("causeError").recoverWith(
        {
          case t: Exception => askPong("Ping")
        }
      )
    }

    it("List[Future] to Future[List]") {
      val listOfFutures: List[Future[String]] = List("ping", "Pong", "failed").map(x => askPong(x))
      val futureOfList: Future[List[String]] = Future.sequence(
        listOfFutures.map(
          future => future.recover {
            case t: Exception => "exception"
          }))
      val result = Await.result(futureOfList.mapTo[List[String]], 1 second)
      println(futureOfList.mapTo[List[String]])
    }

  }


}
