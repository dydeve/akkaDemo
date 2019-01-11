package chapter05.akkademy

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * @Description:
  * @Date 下午5:48 2019/1/11
  * @Author: joker
  */
object ArticleParseFuture {

  import scala.concurrent.ExecutionContext.Implicits.global

  def parse(htmls: List[String]): Future[List[String]] = {
    val futures = htmls.map(html => Future(ArticleParser(html)))
    Future.sequence(futures)
  }

  def parsed(htmls: List[String], atMost: Duration): List[String] = {
    val futures = htmls.map(html => Future(ArticleParser(html)))
    val listInFuture = Future.sequence(
      futures.map {
        future =>
          future.recover {
            case e: Exception => "sorry!"
          }
      }
    )

    Await.result(listInFuture, atMost)

  }

}
