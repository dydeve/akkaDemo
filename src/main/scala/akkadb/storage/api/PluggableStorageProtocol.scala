package akkadb.storage.api

import java.util.UUID

import akkadb.storage.api.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData}

import scala.concurrent.Future

/**
  * @Description:
  * @Date 下午4:09 2019/1/25
  * @Author: joker
  */
trait GetStorageProtocol {
  def get(id: UUID)(resolveOriginality: UUID => DataOriginality): Future[StorageGetData]
}

trait PutStorageProtocol {
  def put(data: Data)(resolveOriginality: UUID => DataOriginality): Future[Ack]
}


trait PluggableStorageProtocol extends GetStorageProtocol with PutStorageProtocol

object PluggableStorageProtocol {

  sealed trait StorageGetData

  object StorageGetData {

    case class Single(data: Data) extends StorageGetData

    case object None extends StorageGetData

  }

  sealed trait Ack

  case object Ack extends Ack {
    val future: Future[Ack] = Future.successful(Ack)
  }

  sealed trait DataOriginality {
    def ringPartitionId: RingPartitionId
  }

  object DataOriginality {
    case class Primary(ringPartitionId: RingPartitionId) extends DataOriginality
    case class Replica(ringPartitionId: RingPartitionId) extends DataOriginality
  }

}
