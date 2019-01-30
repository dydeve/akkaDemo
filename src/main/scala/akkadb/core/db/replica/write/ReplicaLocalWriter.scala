package akkadb.core.db.replica.write

import akkadb.core.db.Data
import akkadb.core.db.actor.protocol.{StorageNodeConflictedWrite, StorageNodeFailedWrite, StorageNodeSuccessfulWrite, StorageNodeWriteResponse}
import akkadb.core.db.replica.IsPrimaryOrReplica
import akkadb.storage.api.PluggableStorageProtocol.StorageGetData
import akkadb.storage.api.{GetStorageProtocol, PutStorageProtocol}
import akkadb.vectorclock.{VectorClockComparator, VectorClockRelation}

import scala.concurrent.{ExecutionContext, Future}

class ReplicaLocalWriter(storage: GetStorageProtocol with PutStorageProtocol)(implicit ec: ExecutionContext) {

  def apply(newData: Data, isPrimaryOrReplica: IsPrimaryOrReplica): Future[StorageNodeWriteResponse] = {
    storage.get(newData.id)(isPrimaryOrReplica).flatMap {
      case StorageGetData.None            => putSingleSuccessfulWrite(newData, isPrimaryOrReplica)
      case StorageGetData.Single(oldData) => handleExistedSingleData(oldData, newData, isPrimaryOrReplica)
    } recover { case _                    => StorageNodeFailedWrite(newData.id) }
  }

  private def handleExistedSingleData(oldData: Data, newData: Data, isPrimaryOrReplica: IsPrimaryOrReplica) = {
    new VectorClockComparator().apply(oldData.vclock, newData.vclock) match {
      case VectorClockRelation.Predecessor => Future.successful(StorageNodeFailedWrite(newData.id))
      case VectorClockRelation.Conflict    => Future.successful(StorageNodeConflictedWrite(oldData, newData))
      case VectorClockRelation.Consequent  => putSingleSuccessfulWrite(newData, isPrimaryOrReplica)
    }
  }

  private def putSingleSuccessfulWrite(newData: Data, resolveDataOriginality: IsPrimaryOrReplica) = {
    storage.put(newData)(resolveDataOriginality).map(_ => StorageNodeSuccessfulWrite(newData.id))
  }
}

