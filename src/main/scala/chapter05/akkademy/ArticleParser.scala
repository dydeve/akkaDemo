package chapter05.akkademy

import de.l3s.boilerpipe.extractors.ArticleExtractor

/**
  * @Description:
  * @Date 下午6:10 2019/1/11
  * @Author: joker
  */
object ArticleParser {
  def apply(html: String): String = ArticleExtractor.INSTANCE.getText(html)
}
