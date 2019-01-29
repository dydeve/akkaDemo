package akkadb.core.db.kryo

import java.util.UUID

import akkadb.core.db.actor.protocol.StorageNodeLocalRead
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

/**
  * @Description:
  * @Date 下午3:58 2019/1/29
  * @Author: joker
  */
object StorageNodeLocalReadSerializer extends Serializer[StorageNodeLocalRead] {

  override def write(kryo: Kryo, output: Output, localRead: StorageNodeLocalRead): Unit = {
    output.writeString(localRead.id.toString)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeLocalRead]): StorageNodeLocalRead = {
    StorageNodeLocalRead(UUID.fromString(input.readString()))
  }
}