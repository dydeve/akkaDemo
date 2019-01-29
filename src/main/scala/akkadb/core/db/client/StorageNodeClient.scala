package akkadb.core.db.client

import java.util.UUID

import akkadb.core.db.Data
import akkadb.core.db.replica.{R, W}

import scala.concurrent.Future

trait StorageNodeClient {
  def get(id: UUID, r: R): Future[GetValueResponse]
  def write(data: Data, w: W): Future[WriteValueResponse]
}

sealed trait GetValueResponse
object GetValueResponse {
  case class Found(data: Data) extends GetValueResponse
  case class Conflicts(data: List[Data]) extends GetValueResponse
  case class NotFound(id: UUID) extends GetValueResponse
  case class Failure(error: String) extends GetValueResponse
}

sealed trait WriteValueResponse
object WriteValueResponse {
  case class Success(id: UUID) extends WriteValueResponse
  case object Conflict extends WriteValueResponse
  case class Failure(error: String) extends WriteValueResponse
}
