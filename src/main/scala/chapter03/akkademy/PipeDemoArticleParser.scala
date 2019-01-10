package chapter03.akkademy

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import akka.pattern.ask
import akka.pattern.pipe

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PipeDemoArticleParser(
                                    cacheActorPath: String,
                                    httpClientActorPath: String,
                                    articleParserActorPath: String,
                                    implicit val timeout: Timeout
                                  ) extends Actor with ActorLogging {

  val cacheActor = context.actorSelection(cacheActorPath)
  val httpClientActor = context.actorSelection(httpClientActorPath)
  val articleParserActor = context.actorSelection(articleParserActorPath)

  import scala.concurrent.ExecutionContext.Implicits.global


  override def receive: Receive = {
    case ParseArticle(uri) =>
      val cacheResult = cacheActor ? GetRequest(uri)
      val result = cacheResult.recoverWith {
        case _: Exception =>
          val fRawResult = httpClientActor ? uri //visit remote http
          fRawResult flatMap {
            case HttpResponse(rawArticle) =>
              articleParserActor ? ParseHtmlArticle(uri, rawArticle) //parse the http response
            case x =>
              Future.failed(new Exception("unknown response"))
          }
      }

      //result pipeTo
      val transform = result.transform {
        case Success(x: String) =>
          log.info("cached result!")
          Success(x)
        case Success(x: ArticleBody) =>
          cacheActor ! SetRequest(uri, x.body)
          Success(x.body)
        case Failure(e) =>
          Failure(e)
        case x =>
          Failure(new IllegalArgumentException(x.getClass.getName))
      }

      transform pipeTo sender()
  }
}
