package akkadb.storage.api

import java.util.UUID

/**
  * @Description:
  * @Date 下午4:04 2019/1/25
  * @Author: joker
  */
case class Data(id: UUID, value: String, vclock: Base64, timestamp: Long)
