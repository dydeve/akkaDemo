package akkadb.core.db.merkletree

import scala.annotation.tailrec
import scala.util.Try

case class MerkleNodeId(id: Int) extends AnyVal

sealed trait MerkleTree {
  def nodeId: MerkleNodeId

  def digest: Digest
}

case class MerkleHashNode(nodeId: MerkleNodeId, digest: Digest, left: MerkleTree, right: MerkleTree) extends MerkleTree

case class MerkleLeaf(nodeId: MerkleNodeId, digest: Digest) extends MerkleTree

object MerkleTree {

  def unapply(blocks: Seq[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = unapply(blocks.toArray)

  def unapply(blocks: Array[Block])(implicit ev: MerkleDigest[Block]): Option[MerkleTree] = {

    sealed trait TempMerkleTree {
      def digest: Digest
    }

    case class TempMerkleHashNode(digest: Digest, left: TempMerkleTree, right: TempMerkleTree) extends TempMerkleTree
    case class TempMerkleLeaf(digest: Digest) extends TempMerkleTree

    def blockToLeaf(b: Block): TempMerkleLeaf = TempMerkleLeaf(ev.digest(b))

    def buildTree(blocks: Array[Block]) = Try {
      val leafs = blocks.map(blockToLeaf)
      var trees: Seq[TempMerkleTree] = leafs.toSeq

      while (trees.length > 1) {
        trees = trees.grouped(2)
            .map(x => mergeTrees(x(0), x(1)))
            .toSeq
      }
      trees.head
    }

    def mergeTrees(tree: TempMerkleTree, tree1: TempMerkleTree) = {
      val mergedDigest = tree.digest + tree1.digest
      val hash = ev.digest(mergedDigest.hash)
      TempMerkleHashNode(hash, tree, tree1)
    }

    def toFinalForm(tmt: TempMerkleTree): MerkleTree = {
      var counter = -1

      def toMerkle(mt: TempMerkleTree): MerkleTree = {
        counter += 1
        mt match {
          case TempMerkleHashNode(digest, left, right) => MerkleHashNode(MerkleNodeId(counter), digest, toMerkle(left), toMerkle(right))
          case TempMerkleLeaf(digest) => MerkleLeaf(MerkleNodeId(counter), digest)
        }
      }

      toMerkle(tmt)
    }

    buildTree(blocks ++ zeroed(blocks))
      .toOption
      .map(toFinalForm)

  }

  def zeroed(blocks: Seq[Block]): Array[Array[Byte]] = {
    def zero(i: Int): Int = {
      val factor = 2
      var x = factor
      while (x < i) x *= factor
      x - i
    }

    Array.fill(zero(blocks.length))(Array[Byte](0))
  }

  @tailrec
  def findNode(nodeId: MerkleNodeId, merkleTree: MerkleTree): Option[MerkleTree] = {
    merkleTree match {
      case _ if merkleTree.nodeId == nodeId => Option(merkleTree)
      case MerkleHashNode(_, _, _, right) if nodeId.id >= right.nodeId.id => findNode(nodeId, right)
      case MerkleHashNode(_, _, left, _) => findNode(nodeId, left)
      case _ => None
    }
  }

}
