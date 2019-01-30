package akkadb.core.db.replica.read

import java.util.UUID

import akka.util.Timeout
import akkadb.core.db.actor.StorageNodeActorRef
import akkadb.core.db.actor.protocol.{StorageNodeFailedRead, StorageNodeLocalRead, StorageNodeReadResponse}
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ReplicaRemoteReader(implicit ec: ExecutionContext) {

  private implicit val timeout = Timeout(3.seconds) // TODO: tune this value

  def apply(storageNodeRefs: List[StorageNodeActorRef], id: UUID): Future[List[StorageNodeReadResponse]] = {
    Future.sequence(storageNodeRefs.map(getValue(_, id)))
  }

  private def getValue(node: StorageNodeActorRef, id: UUID): Future[StorageNodeReadResponse] = {
    (node.ref ? StorageNodeLocalRead(id))
      .mapTo[StorageNodeReadResponse]
      .recover { case _ => StorageNodeFailedRead(id) }
  }
}

