package akkadb.storage.api.provider

import akkadb.storage.api.PluggableStorageProtocol

/**
  * @Description:
  * @Date 下午4:53 2019/1/25
  * @Author: joker
  */
trait StorageProvider {
  def name: String
  def init: PluggableStorageProtocol
}

object StorageProvider {
  def apply(clazz: String): StorageProvider = Class.forName(clazz).newInstance().asInstanceOf[StorageProvider]
}
