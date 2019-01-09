package chapter03

import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props, Status}
import akka.testkit.TestProbe
import akka.util.Timeout
import chapter03.akkademy._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import akka.pattern.ask
import de.l3s.boilerpipe.extractors.ArticleExtractor

import scala.concurrent.{Await, TimeoutException}
import scala.util.Success

/**
  * @Description:
  * @Date 下午5:42 2019/1/9
  * @Author: joker
  */
class AskAndTellDemoSpec extends FunSpec with Matchers {

  implicit val system = ActorSystem("test")
  implicit val timeout = Timeout(4 seconds)

  describe("ask demo") {
    val cacheProbe = TestProbe()
    val httpClientProbe = TestProbe()
    val articleParseActor = system.actorOf(Props[ParsingActor])
    val askDemoActor = system.actorOf(
      Props(
        classOf[AskDemoArticleParser],//构造函数
        cacheProbe.ref.path.toString,
        httpClientProbe.ref.path.toString,
        articleParseActor.path.toString,
        timeout
      )
    )

    it("should provide parsed article") {
      val f = askDemoActor ? ParseArticle("https://google.com")

      //cache gets the message first, fail cache result
      cacheProbe.expectMsgType[GetRequest]
      cacheProbe.reply(Status.Failure(new Exception("no cache")))

      //if cache fails, then http client gets a request
      httpClientProbe.expectMsgType[String]
      httpClientProbe.reply(HttpResponse(Articles.article1))

      cacheProbe.expectMsgType[SetRequest]

      val parsedArticle = Await.result(f.mapTo[String], 10 second)

      parsedArticle should include("I’ve been writing a lot in emacs lately")
      parsedArticle.toString should not include("<body>")
    }
  }

  describe("tell demo") {
    val cacheProbe = TestProbe()
    val httpClientProbe = TestProbe()
    val articleParseActor = system.actorOf(Props[ParsingActor])
    val tellDemoActor = system.actorOf(
      Props(classOf[TellDemoArticleParser],
        cacheProbe.ref.path.toString,
        httpClientProbe.ref.path.toString,
        articleParseActor.path.toString,
        timeout)
    )
    it("should provide parsed article") {
      val f = tellDemoActor ? ParseArticle("http://www.google.com")

      //Cache gets the message first. Fail cache request.
      cacheProbe.expectMsgType[GetRequest]
      cacheProbe.reply(Failure(new Exception("no cache")))

      //if it fails, http client gets a request
      httpClientProbe.expectMsgType[String]
      httpClientProbe.reply(HttpResponse(Articles.article1))

      cacheProbe.expectMsgType[SetRequest] //Article will be cached.

      val parsedArticle = Await.result(f, 10 seconds)
      parsedArticle.toString should include("I’ve been writing a lot in emacs lately")
      parsedArticle.toString should not include("<body>")
    }

    it("should provide cached article") {
      val f = tellDemoActor ? ParseArticle("http://www.google.com")

      //Cache gets the message first. Fail cache request.
      cacheProbe.expectMsgType[GetRequest]
      cacheProbe.reply(de.l3s.boilerpipe.extractors.ArticleExtractor.INSTANCE.getText(Articles.article1)
      )

      val parsedArticle = Await.result(f, 10 seconds)
      parsedArticle.toString should include("I’ve been writing a lot in emacs lately")
      parsedArticle.toString should not include("<body>")
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  describe("test improved tell") {
    val cacheProbe = TestProbe()
    val httpClientProbe = TestProbe()
    val articleParseActor = system.actorOf(Props[ParsingActor])
    val tellDemoActor = system.actorOf(
      Props(classOf[ImprovedTellDemoArticleParser],
        cacheProbe.ref.path.toString,
        httpClientProbe.ref.path.toString,
        articleParseActor.path.toString,
        timeout)
    )

    it("cache not hit, then http, parse, cache") {
      val f = tellDemoActor ? ParseArticle("http://google.com")
      cacheProbe.expectMsgType[GetRequest]
      cacheProbe.reply(KeyNotFoundException("http://google.com"))

      httpClientProbe.expectMsg("http://google.com")
      httpClientProbe.reply(HttpResponse(Articles.article1))

      cacheProbe.expectMsgType[SetRequest]
      val result = Await.result(f.mapTo[String], 1 second)

      result should include("I’ve been writing a lot in emacs lately")
      result should not include("<body>")
    }

    it("cache hit") {
      val f = tellDemoActor ? ParseArticle("http://google.com")
      cacheProbe.expectMsgType[GetRequest]
      cacheProbe.reply(ArticleExtractor.INSTANCE.getText(Articles.article1))

      val result = Await.result(f.mapTo[String], 1 second)

      result should include("I’ve been writing a lot in emacs lately")
      result should not include("<body>")
    }

    it("timeout") {
      //val f = tellDemoActor ? ParseArticle("http://google.com") ?本身有timeout，不能测出improved...的"timeout"
//      val newF = f onComplete {
//        case Success(value: Status.Failure) =>
//          println(value.cause)
//          value
//        case scala.util.Failure(exception) =>
//          println(exception)
//          exception
//      }

      val testProbe = TestProbe()
      tellDemoActor.tell(ParseArticle("123"), testProbe.ref)

      //exception -- testProbe.expectMsg(10 second, Status.Failure(new TimeoutException("timeout")))
      //testProbe.expectMsgType[Failure](5 second)

      val a = testProbe.receiveOne(5 second)
      println(a)
    }

  }

}
