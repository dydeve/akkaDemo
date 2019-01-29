package akkadb.core.db.kryo

import java.util.UUID

import akkadb.core.db.Data
import akkadb.core.db.versioning.NodeIdVectorClockBase64
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

object DataSerializer extends Serializer[Data] {
  override def write(kryo: Kryo, output: Output, data: Data): Unit = {
    output.writeString(data.id.toString)//uuid
    output.writeString(data.value)//value
    output.writeString(new NodeIdVectorClockBase64().encode(data.vclock).get)//vector clock
    output.writeLong(data.timestamp)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[Data]): Data = {
    Data(
      id = UUID.fromString(input.readString()),
      value = input.readString(),
      vclock = new NodeIdVectorClockBase64().decode(input.readString()).get,
      timestamp = input.readLong()
    )
  }
}
