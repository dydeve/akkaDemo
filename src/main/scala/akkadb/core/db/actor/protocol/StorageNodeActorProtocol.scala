package akkadb.core.db.actor.protocol

import java.util.UUID

import akkadb.core.db.Data
import akkadb.core.db.replica.{R, W}
import akkadb.storage.api.AkkaDbData

sealed trait StorageNodeReadRequest
case class StorageNodeLocalRead(id: UUID) extends StorageNodeReadRequest

sealed trait StorageNodeReadResponse
case class StorageNodeFoundRead(data: Data) extends StorageNodeReadResponse
case class StorageNodeConflictedRead(conflicts: List[Data]) extends StorageNodeReadResponse
case class StorageNodeNotFoundRead(id: UUID) extends StorageNodeReadResponse
case class StorageNodeFailedRead(id: UUID) extends StorageNodeReadResponse

sealed trait StorageNodeWriteRequest
case class StorageNodeWriteDataLocal(data: Data) extends StorageNodeWriteRequest

sealed trait StorageNodeWriteResponse
case class StorageNodeSuccessfulWrite(id: UUID) extends StorageNodeReadRequest
case class StorageNodeFailedWrite(id: UUID) extends StorageNodeWriteResponse
case class StorageNodeConflictedWrite(oldData: AkkaDbData, newData: AkkaDbData) extends StorageNodeWriteResponse

//these message are not sent over remote channel
object Internal {
  case class ReadReplica(r: R, id: UUID) extends StorageNodeReadRequest
  case class WriteReplica(w: W, data: AkkaDbData) extends StorageNodeWriteRequest
}

