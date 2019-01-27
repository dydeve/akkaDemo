package akkadb.storage.mem.provider

import akkadb.storage.api.PluggableStorageProtocol
import akkadb.storage.api.provider.StorageProvider
import akkadb.storage.mem.InMemStorage
import akkadb.storage.mem.config.StorageConfig

/**
  * @Description:
  * @Date 下午5:08 2019/1/25
  * @Author: joker
  */
class InMemStorageProvider extends StorageProvider {
  override def name: String = StorageConfig.storage.inmemory.name

  override def init: PluggableStorageProtocol = new InMemStorage
}
