package chapter03.akkademy

import akka.actor.{Actor, ActorLogging}
import de.l3s.boilerpipe.extractors.ArticleExtractor

/**
  * 解析文章
  * @Date 下午5:25 2019/1/9
  * @Author: joker
  */
class ParsingActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case ParseHtmlArticle(uri, html) =>
      sender() ! ArticleBody(uri, ArticleExtractor.INSTANCE.getText(html))
    case x =>
      log.info("unknown message:{}", x.getClass)
  }

}
