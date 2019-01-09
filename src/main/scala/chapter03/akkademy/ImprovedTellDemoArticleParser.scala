package chapter03.akkademy

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import akka.util.Timeout

import scala.concurrent.TimeoutException

/**
  * @Description:
  * @Date 上午2:12 2019/1/10
  * @Author: joker
  */
class ImprovedTellDemoArticleParser(
                                     cacheActorPath: String,
                                     httpClientActorPath: String,
                                     articleParserActorPath: String,
                                     implicit val timeout: Timeout
                                   ) extends Actor with ActorLogging {

  val cacheActor = context.actorSelection(cacheActorPath)
  val httpClientActor = context.actorSelection(httpClientActorPath)
  val articleParserActor = context.actorSelection(articleParserActorPath)

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case ParseArticle(url) =>
      val extraActor = buildExtraActor(sender(), url)
      cacheActor.tell(GetRequest(url), extraActor)

      context.system.scheduler.scheduleOnce(timeout.duration, extraActor, "timeout")
  }

  private def buildExtraActor(senderRef: ActorRef, uri: String): ActorRef = {
    context.actorOf(Props(new Actor {
      override def receive: Receive = {
        case "timeout" => //if we get timeout, then fail
          senderRef ! Status.Failure(new TimeoutException("timeout"))
          context.stop(self)
        //get from cache
        case body: String =>
          senderRef ! body
          context.stop(self)
        case KeyNotFoundException(uri) =>
          httpClientActor ! uri
        //收到body,解析body
        case HttpResponse(body) =>
          articleParserActor ! ParseHtmlArticle(uri, body)

        case ArticleBody(uri, body) => // `uri` 不可以，分布式并发环境
          cacheActor ! SetRequest(uri, body)
          senderRef ! body
          context.stop(self)

        case t =>
          log.info("unknown message:{}", t.getClass)
      }
    }))
  }
}
