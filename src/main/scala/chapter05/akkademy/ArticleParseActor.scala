package chapter05.akkademy

import akka.actor.{Actor, Status}

/**
  * @Description:
  * @Date 下午5:46 2019/1/11
  * @Author: joker
  */
case class ParseArticle(html: String)

class ArticleParseActor extends Actor {
  override def receive: Receive = {
    /*
    运行期擦除
    case list: List[ParseArticle] =>
      list.foreach(article => parse(article.html))*/
    case x: List[_] =>
      x.foreach {
        case article: ParseArticle => parse(article.html)
      }
    case ParseArticle(html) =>
      parse(html)
    case _ =>
      sender() ! Status.Failure(new IllegalArgumentException)
  }

  def parse(html: String) = {
    val parsed: String = ArticleParser(html)
    sender() ! parsed
  }
}

