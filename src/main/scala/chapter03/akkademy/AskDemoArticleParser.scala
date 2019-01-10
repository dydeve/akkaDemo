package chapter03.akkademy

import akka.actor.{Actor, ActorLogging, Status}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future

/**
  * @Description:
  * @Date 下午4:37 2019/1/9
  * @Author: joker
  */
class AskDemoArticleParser(cacheActorPath: String,
                           httpClientActorPath: String,
                           articleParserActorPath: String,
                           implicit val timeout: Timeout
                          ) extends Actor with ActorLogging {

  val cacheActor = context.actorSelection(cacheActorPath)
  val httpClientActor = context.actorSelection(httpClientActorPath)
  val articleParserActor = context.actorSelection(articleParserActorPath)

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
    * Note there are 3 asks so this potentially creates 6 extra objects:
    * - 3 Promises
    * - 3 Extra actors
    */
  override def receive: Receive = {
    case ParseArticle(uri) =>

      /**
        * 匿名函数是在一个不同的线程中执行的，有着不同的 执行上下文，因此在匿名函数中的代码块里调用 sender()方法时，返回值是不可预知的
        * 有更好的办法可以处理这个问题，叫做Pipe
        */
      //sender ref needed for closure,need to use in callback (see Pipe pattern for better solution)
      val senderRef = sender()

      val cacheResult = cacheActor ? GetRequest(uri)

      val result = cacheResult.recoverWith {
        case _: Exception =>
          val fRawResult = httpClientActor ? uri//visit remote http
          fRawResult flatMap {
            case HttpResponse(rawArticle) =>
              articleParserActor ? ParseHtmlArticle(uri, rawArticle)//parse the http response
            case x =>
              Future.failed(new Exception("unknown response"))
          }
      }

      result onComplete {
        case scala.util.Success(x: String) =>
          log.info("cached result!")

          /**
            * sender() ! value  被异步使用了，不是这个 actor
            *
            * Ask timed out on [Actor[akka://test/user/$b#-1834188917]] after [10000 ms]. Message of type [chapter03.akkademy.ParseArticle]. A typical reason for `AskTimeoutException` is that the recipient actor didn't send a reply.
            * akka.pattern.AskTimeoutException: Ask timed out on [Actor[akka://test/user/$b#-1834188917]] after [10000 ms]. Message of type [chapter03.akkademy.ParseArticle]. A typical reason for `AskTimeoutException` is that the recipient actor didn't send a reply.
            */
          senderRef ! x //返回缓存结果

        case scala.util.Success(x: ArticleBody) =>
          //设置缓存
          cacheActor ! SetRequest(uri, x.body)
          senderRef ! x.body
          //sender ! x.body

        case scala.util.Failure(e) =>
          senderRef ! Status.Failure(e)

        case x =>
          log.info("unknown message:{}", x)
      }

  }
}
