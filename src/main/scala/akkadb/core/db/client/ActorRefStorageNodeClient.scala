package akkadb.core.db.client

import java.util.UUID

import akkadb.core.db.actor.StorageNodeActorRef
import akkadb.core.db.actor.protocol._
import akkadb.core.db.replica.{R, W}
import akka.pattern.ask
import akka.util.Timeout
import akkadb.core.db.Data

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * @Description:
  * @Date 下午2:05 2019/1/28
  * @Author: joker
  */
class ActorRefStorageNodeClient(storageNodeActor: StorageNodeActorRef)(implicit ex: ExecutionContext) {

  implicit val timeout = Timeout(5 second)//todo tune this value

  def get(id: UUID, r: R): Future[GetValueResponse] = {
    (storageNodeActor.ref ? Internal.ReadReplica(r, id)).mapTo[StorageNodeReadResponse].map {
      case StorageNodeFoundRead(data) => GetValueResponse.Found(data)
      case StorageNodeConflictedRead(data) => GetValueResponse.Conflicts(data)
      case StorageNodeNotFoundRead(id)     => GetValueResponse.NotFound(id)
      case StorageNodeFailedRead(_)        => GetValueResponse.Failure(s"Couldn't read value with id ${id.toString}")
    } recover { case e: Throwable =>
      GetValueResponse.Failure(s"Unsuccessful read of value with id ${id.toString}")
    }
  }

  def write(data: Data, w: W): Future[WriteValueResponse] = {
    (storageNodeActor.ref ? Internal.WriteReplica(w, data)).mapTo[StorageNodeWriteResponse].map {
      case StorageNodeSuccessfulWrite(id)   => WriteValueResponse.Success(id)
      case StorageNodeConflictedWrite(_, _) => WriteValueResponse.Conflict
      case StorageNodeFailedWrite(id)       => WriteValueResponse.Failure(s"Couldn't write value with id ${id.toString}")
    } recover { case ex: Throwable          => WriteValueResponse.Failure(s"Unsuccessful write of value with id ${data.id.toString}") }
  }

}
