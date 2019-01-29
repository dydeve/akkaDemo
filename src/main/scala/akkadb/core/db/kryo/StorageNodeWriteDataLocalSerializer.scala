package akkadb.core.db.kryo

import java.util.UUID

import akkadb.core.db.Data
import akkadb.core.db.actor.protocol.StorageNodeWriteDataLocal
import akkadb.core.db.versioning.NodeIdVectorClockBase64
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

object StorageNodeWriteDataLocalSerializer extends Serializer[StorageNodeWriteDataLocal] {
  override def write(kryo: Kryo, output: Output, local: StorageNodeWriteDataLocal): Unit = {
    output.writeString(local.data.id.toString) // UUID
    output.writeString(local.data.value)       // Value
    output.writeString(new NodeIdVectorClockBase64().encode(local.data.vclock).get)  // Vector Clock
    output.writeLong(local.data.timestamp)     // Timestamp
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[StorageNodeWriteDataLocal]): StorageNodeWriteDataLocal = {
    val id = UUID.fromString(input.readString()) // UUID
    val value = input.readString()               // Value
    val vectorClock = new NodeIdVectorClockBase64().decode(input.readString()).get // Vector Clock
    val timestamp = input.readLong()             // Timestamp

    StorageNodeWriteDataLocal(Data(id, value, vectorClock, timestamp))
  }
}
