package akkadb.storage.mem.config

/**
  * @Description:
  * @Date 下午5:05 2019/1/25
  * @Author: joker
  */
trait storage {
  val storage = new {
    val inmemory = new {
      val name: String = "In-Mem Storage"
    }
  }
}
object StorageConfig extends storage
