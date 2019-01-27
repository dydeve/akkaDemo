package akkadb.storage.mem

import java.util.UUID

import akkadb.storage.api.PluggableStorageProtocol.{Ack, DataOriginality, StorageGetData}
import akkadb.storage.api.{Data, PluggableStorageProtocol, RingPartitionId}

import scala.collection.mutable
import scala.concurrent.Future

/**
  * @Description:
  * @Date 下午5:03 2019/1/25
  * @Author: joker
  */
class InMemStorage extends PluggableStorageProtocol {

  private type MMap = mutable.Map[RingPartitionId, Map[UUID, Data]]
  private var primaries: MMap = mutable.Map.empty[RingPartitionId, Map[UUID, Data]]
  private var replicas: MMap = mutable.Map.empty[RingPartitionId, Map[UUID, Data]]

  override def put(data: Data)(resolveOriginality: UUID => DataOriginality): Future[Ack] = {
    def update(mmap: MMap, partitionId: RingPartitionId, data: Data) = {
      mmap.get(partitionId) match {
        //case Some(partitionMap) => mmap + (partitionId -> (partitionMap ++ Map(data.id -> data)))
        case Some(partitionMap) => mmap + (partitionId -> (partitionMap + (data.id -> data)))
        case None => mmap + (partitionId -> Map(data.id -> data))
      }
    }

    resolveOriginality(data.id) match {
      case DataOriginality.Primary(partitionId) => primaries = update(primaries, partitionId, data)
      case DataOriginality.Replica(partitionId) => replicas = update(replicas, partitionId, data)
    }

    Ack.future
  }

  override def get(id: UUID)(resolveOriginality: UUID => DataOriginality): Future[StorageGetData] = Future.successful {
    def get(mmap: MMap, partitionId: RingPartitionId) = {
      mmap.get(partitionId).fold[StorageGetData](StorageGetData.None) {
        _.get(id) match {
          case Some(data) => StorageGetData.Single(data)
          case None => StorageGetData.None
        }
      }
    }

    resolveOriginality(id) match {
      case DataOriginality.Primary(partitionId) => get(primaries, partitionId)
      case DataOriginality.Replica(partitionId) => get(replicas, partitionId)
    }
  }

}
