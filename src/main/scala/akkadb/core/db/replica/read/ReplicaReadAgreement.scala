package akkadb.core.db.replica.read

import akkadb.consistenthashing.NodeId
import akkadb.core.db.actor.protocol.{StorageNodeFailedRead, StorageNodeFoundRead, StorageNodeNotFoundRead, StorageNodeReadResponse}
import akkadb.core.db.replica.R
import akkadb.vectorclock.{VectorClockComparator, VectorClockRelation}

/**
  * @Description:
  * @Date 上午10:27 2019/1/30
  * @Author: joker
  */
class ReplicaReadAgreement {

  def reach(r: R): List[StorageNodeReadResponse] => ReadAgreement = { reads =>
    if(areAllNotFound(reads)) {
      ReadAgreement.AllNotFound
    } else if(areAllFailed(reads)) {
      ReadAgreement.AllFailed
    } else {
      val onlyFoundReads = collectFound(reads)
      (onlyFoundReads.size >= r.r, onlyFoundReads.size == 1, hasSameVC(onlyFoundReads), foundOnlyConsequent(onlyFoundReads)) match {
        case (true, true, _, _)                 => ReadAgreement.Found(onlyFoundReads.head.data)
        case (true, false, true, _)             => ReadAgreement.Found(onlyFoundReads.head.data)
        case (true, false, _, c) if c.size == 1 => ReadAgreement.Consequent(c.head._1)
        case (true, false, _, _)                => ReadAgreement.Conflicts(onlyFoundReads.map(_.data))
        case (false, _, _, _)                   => ReadAgreement.NotEnoughFound
      }
    }
  }

  private def areAllNotFound(reads: List[StorageNodeReadResponse]) = reads.collect { case nf: StorageNodeNotFoundRead => nf }.size == reads.size

  private def areAllFailed(reads: List[StorageNodeReadResponse]) = reads.collect { case fr: StorageNodeFailedRead => fr }.size == reads.size

  private def collectFound(reads: List[StorageNodeReadResponse]) = reads.collect { case r: StorageNodeFoundRead => r }

  private def hasSameVC(onlyFoundReads: List[StorageNodeFoundRead]) = onlyFoundReads.map(_.data.vclock).distinct.size == 1

  private def foundOnlyConsequent(onlyFoundReads: List[StorageNodeFoundRead]) = {
    val vcComparator = new VectorClockComparator[NodeId]

    onlyFoundReads.flatMap { compared =>
      onlyFoundReads.filterNot(_ == compared)
        .map(base => (compared.data, vcComparator.apply(base.data.vclock, compared.data.vclock)))
        .groupBy { case (data, _) => data }
        .filter { case (_, l) => l.forall { case (_, relation) => relation == VectorClockRelation.Consequent }}
    }
  }
}
