package akkadb.core.db

import java.util.UUID

import akkadb.consistenthashing.NodeId
import akkadb.core.db.replica.PreferenceList
import akkadb.core.db.versioning.NodeIdVectorClockBase64
import akkadb.storage.api.AkkaDbData
import akkadb.vectorclock.VectorClock

/**
  * @Description:
  * @Date 下午2:53 2019/1/29
  * @Author: joker
  */
case class Data(id: UUID, value: String, vclock: VectorClock[NodeId] = VectorClock(), timestamp: Long = System.currentTimeMillis())

object Data {
  def updateClock(data: Data, preferenceList: PreferenceList): Data = {
    val nodeIds = preferenceList.all
    data.copy(vclock = nodeIds.foldLeft(data.vclock)(_ increase _))
  }

  implicit def toInternal(data: Data): AkkaDbData = {
    val encodedVClock = new NodeIdVectorClockBase64().encode(data.vclock).get
    AkkaDbData(data.id, data.value, encodedVClock, data.timestamp)
  }

  implicit def fromInternal(akkaDbData: AkkaDbData): Data = {
    val decodedVClock = new NodeIdVectorClockBase64().decode(akkaDbData.vclock).get
    Data(akkaDbData.id, akkaDbData.value, decodedVClock, akkaDbData.timestamp)

  }
}
