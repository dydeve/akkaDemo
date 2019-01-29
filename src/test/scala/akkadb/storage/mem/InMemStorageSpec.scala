package akkadb.storage.mem

import java.util.UUID

import akkadb.storage.api.AkkaDbData
import akkadb.storage.api.PluggableStorageProtocol.{DataOriginality, StorageGetData}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @Description:
  * @Date 下午7:02 2019/1/25
  * @Author: joker
  */
class InMemStorageSpec extends FunSpec with Matchers {
  private def prepareData(id: UUID, value: String) = AkkaDbData(id, value, "", 1L)
  describe("in memory storage") {

    it("store single data") {
      val data = prepareData(UUID.randomUUID(), "some-value")
      val inMemStorage = new InMemStorage
      val resolver = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)

      Await.result(inMemStorage.put(data)(resolver), atMost = 5 seconds)

      //when
      val result = Await.result(inMemStorage.get(data.id)(resolver), atMost = 5 second)
      //then
      result shouldBe StorageGetData.Single(data)
    }


    it("get none data for not existing id in memory") {
      // given
      val noExistingId = UUID.randomUUID()
      val inMemStorage = new InMemStorage
      val resolver     = (id: UUID) => DataOriginality.Primary(ringPartitionId = 1)
      val otherData    = prepareData(id = UUID.randomUUID(), "some-data")
      Await.result(inMemStorage.put(otherData)(resolver), atMost = 5 seconds)

      // when
      val result = Await.result(inMemStorage.get(noExistingId)(resolver), atMost = 5 seconds)

      // then
      result shouldBe StorageGetData.None
    }

    it("get none data for not existing partitionId") {
      // given
      val uid                       = UUID.randomUUID()
      val noExistingRingPartitionId = 1
      val inMemStorage              = new InMemStorage
      val resolver                  = (id: UUID) => DataOriginality.Replica(ringPartitionId = noExistingRingPartitionId)

      // when
      val result = Await.result(inMemStorage.get(uid)(resolver), atMost = 5 seconds)

      // then
      result shouldBe StorageGetData.None
    }

    it("store and merge many data within under single partitionId") {
      // given
      val id1          = UUID.randomUUID()
      val id2          = UUID.randomUUID()
      val data1        = prepareData(id1, "some-data")
      val data2        = prepareData(id2, "some-data")
      val resolver     = (id: UUID) => DataOriginality.Replica(ringPartitionId = 1)
      val inMemStorage = new InMemStorage

      Await.result(inMemStorage.put(data1)(resolver), atMost = 5 seconds)
      Await.result(inMemStorage.put(data2)(resolver), atMost = 5 seconds)

      // when
      val result1 = Await.result(inMemStorage.get(id1)(resolver), atMost = 5 seconds)
      val result2 = Await.result(inMemStorage.get(id2)(resolver), atMost = 5 seconds)

      // then
      result1 shouldBe StorageGetData.Single(data1)
      result2 shouldBe StorageGetData.Single(data2)
    }
  }

}
