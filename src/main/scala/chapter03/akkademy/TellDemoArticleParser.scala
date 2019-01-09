package chapter03.akkademy

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Status}
import akka.util.Timeout

import scala.concurrent.TimeoutException

/**
  * @Description:
  * @Date 上午1:08 2019/1/10
  * @Author: joker
  */
class TellDemoArticleParser(
                             cacheActorPath: String,
                             httpClientActorPath: String,
                             articleParserActorPath: String,
                             implicit val timeout: Timeout
                           ) extends Actor with ActorLogging {

  val cacheActor = context.actorSelection(cacheActorPath)
  val httpClientActor = context.actorSelection(httpClientActorPath)
  val articleParserActor = context.actorSelection(articleParserActorPath)

  implicit val ec = context.dispatcher

  /**
    * While this example is a bit harder to understand than the ask demo,
    * for extremely performance critical applications, this has an advantage over ask.
    * The creation of 5 objects are saved - only one extra actor is created.
    * Functionally it's similar.
    * It will make the request to the HTTP actor w/o waiting for the cache response though (can be solved).
    *
    * 未创建Future
    * @return
    */
  override def receive: Receive = {
    case ParseArticle(uri) =>
      val extraActor = buildExtraActor(sender(), uri)
      cacheActor.tell(GetRequest(uri), extraActor)
      httpClientActor.tell(uri, extraActor)

      context.system.scheduler.scheduleOnce(timeout.duration, extraActor, "timeout")
  }

  /**
    * The extra actor will collect responses from the assorted actors it interacts with.
    * The cache actor reply, the http actor reply, and the article parser reply are all handled.
    * Then the actor will shut itself down once the work is complete.
    * A great use case for the use of tell here (aka extra pattern) is aggregating data from several sources.
    */
  private def buildExtraActor(senderRef: ActorRef, uri: String): ActorRef = {
    context.actorOf(Props(new Actor {
      override def receive: Receive = {
        case "timeout" => //if we get timeout, then fail
          senderRef ! Status.Failure(new TimeoutException("timeout"))
          context.stop(self)
        //收到body,解析body
        case HttpResponse(body) =>
          articleParserActor ! ParseHtmlArticle(uri, body)
          //get from cache
        case body: String =>
          senderRef ! body
          context.stop(self)
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
