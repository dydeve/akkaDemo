package akkadb.storage.mem

import akkadb.storage.api.provider.StorageProvider
import akkadb.storage.mem.provider.InMemStorageProvider
import org.scalatest.{FunSpec, Matchers}

/**
  * @Description:
  * @Date 下午1:58 2019/1/27
  * @Author: joker
  */
class InMemStorageProviderSpec extends FunSpec with Matchers {
  it("init In-Memory storage") {
    val provider = StorageProvider.apply("justin.db.storage.provider.InMemStorageProvider").asInstanceOf[InMemStorageProvider]

    provider.name shouldBe "In-Mem Storage"
    provider.init shouldBe a[InMemStorage]
  }
}
