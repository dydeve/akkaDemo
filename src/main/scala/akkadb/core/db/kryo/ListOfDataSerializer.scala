package akkadb.core.db.kryo

import akkadb.core.db.Data
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

/**
  * @Description:
  * @Date 下午3:34 2019/1/29
  * @Author: joker
  */
object ListOfDataSerializer extends Serializer[List[Data]]{
  override def write(kryo: Kryo, output: Output, listOfData: List[Data]): Unit = {
    val length = listOfData.size
    output.writeInt(length, true)
    if (length != 0) {
      val it = listOfData.iterator
      while (it.hasNext)
        DataSerializer.write(kryo, output, it.next())
    }
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[List[Data]]): List[Data] = {
    var length = input.readInt(true)
    var result = List.empty[Data]

    while (length > 0) {
      result = result :+ DataSerializer.read(kryo, input, classOf[Data])
      length -= 1
    }
    result
  }
}
