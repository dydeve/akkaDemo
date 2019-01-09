package chapter03.akkademy

/**
  * @Description:
  * @Date 下午4:45 2019/1/9
  * @Author: joker
  */
case class ParseArticle(url: String)

case class ParseHtmlArticle(url: String, htmlString: String)

case class HttpResponse(body: String)

case class ArticleBody(url: String, body: String)

//cache
case class SetRequest(key: String, value: String)

case class GetRequest(key: String)

case class KeyNotFoundException(key: String) extends Exception