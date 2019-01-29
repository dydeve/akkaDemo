package akkadb.core.db.merkletree

import java.nio.ByteBuffer
import java.security.MessageDigest


trait MerkleDigest[T] {
  def digest(t: T): Digest
}

object MerkleDigest {

  implicit object CRC32 extends MerkleDigest[Block] {
    import java.util.zip.CRC32

    override def digest(t: Block): Digest = {
      val digest = new CRC32()
      digest.update(t)

      val buffer = ByteBuffer.allocate(8)
      buffer.putLong(digest.getValue)

      Digest(buffer.array())
    }
  }

  implicit object MD5 extends MerkleDigest[Block] {
    override def digest(t: Block): Digest = {
      Digest(MessageDigest.getInstance("MD5").digest(t))
    }
  }
}

case class Digest(hash: Array[Byte]) extends AnyVal {
  def +(that: Digest): Digest = Digest(this.hash ++ that.hash)
  def ==(that: Digest): Boolean = this.hash.deep == that.hash.deep
}
