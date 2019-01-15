package chapter06

import akka.actor.Status
import common.LoggingActor
import de.l3s.boilerpipe.extractors.ArticleExtractor

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @Description:
  * @Date 下午5:40 2019/1/15
  * @Author: joker
  */
case class ParseArticle(htmlBody: String)

object ArticleParser {

  import scala.concurrent.ExecutionContext.Implicits.global

  def apply(html: String): String = {
    ArticleExtractor.INSTANCE.getText(html)
  }

  def apply0(html: String): Future[String] = {
    Future(ArticleExtractor.INSTANCE.getText(html))
  }
}

class ActorParseActor extends LoggingActor {

  override def receive: Receive = {
    case ParseArticle(htmlBody) =>
      sender() ! ArticleParser(htmlBody)
    /*
    ArticleParser(htmlBody).onComplete {
      case Success(value) =>
        sender() ! value
      case Failure(exception) =>
        sender() ! Status.Failure(exception)
        */
  }

}
