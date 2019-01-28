package akkadb.httpapi

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}
import akkadb.consistenthashing.NodeId
import akkadb.core.db.versioning.NodeIdVectorClockBase64
import akkadb.vectorclock.VectorClock

import scala.util.{Failure, Success, Try}

/**
  * @Description:
  * @Date 下午3:46 2019/1/27
  * @Author: joker
  */
case class VectorClockHeaderException(msg: String) extends Exception(msg)

case class VectorClockHeader(vectorClock: VectorClock[NodeId]) extends ModeledCustomHeader[VectorClockHeader] {
  override def companion: ModeledCustomHeaderCompanion[VectorClockHeader] = VectorClockHeader

  override def value(): String = new NodeIdVectorClockBase64().encode(vectorClock) match {
    case Success(vclock) => vclock
    case Failure(_) => throw VectorClockHeaderException("couldn't encode vector clock of data")
  }

  override def renderInRequests(): Boolean = true

  override def renderInResponses(): Boolean = true
}

object VectorClockHeader extends ModeledCustomHeaderCompanion[VectorClockHeader] {
  override def name: String = "X-Vector-Clock"

  override def parse(value: String): Try[VectorClockHeader] = {
    new NodeIdVectorClockBase64()
      .decode(value)
      .map(VectorClockHeader(_))
  }

  def empty: VectorClockHeader = VectorClockHeader(VectorClock.apply[NodeId]())
}
