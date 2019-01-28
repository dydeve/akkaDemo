package akkadb.core.db.actor.protocol

import akka.actor.ActorRef
import akkadb.core.db.cluster.ClusterMembers

/**
  * @Description:
  * @Date 下午3:42 2019/1/28
  * @Author: joker
  */
case class WriteData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeWriteRequest)
case class ReadData(sender: ActorRef, clusterMembers: ClusterMembers, cmd: StorageNodeReadRequest)
