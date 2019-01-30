package akkadb.core.db.replica.read

import java.util.UUID

import akkadb.core.db.actor.protocol.{StorageNodeFailedRead, StorageNodeFoundRead, StorageNodeNotFoundRead, StorageNodeReadResponse}
import akkadb.core.db.replica.IsPrimaryOrReplica
import akkadb.storage.api.GetStorageProtocol
import akkadb.storage.api.PluggableStorageProtocol.StorageGetData

import scala.concurrent.{ExecutionContext, Future}

/**
  * @Description:
  * @Date 上午10:22 2019/1/30
  * @Author: joker
  */
class ReplicaLocalReader(storage: GetStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(id: UUID, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeReadResponse] = {
    storage.get(id)(isPrimaryOrReplica).map {
      case StorageGetData.Single(data) => StorageNodeFoundRead(data)
      case StorageGetData.None => StorageNodeNotFoundRead(id)
    } recover { case _ => StorageNodeFailedRead(id) }
  }

}
