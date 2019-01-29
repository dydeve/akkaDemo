package akkadb.core.db.kryo

import akkadb.consistenthashing.NodeId
import akkadb.core.db.actor.protocol.RegisterNode
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

/**
  * @Description:
  * @Date 下午3:41 2019/1/29
  * @Author: joker
  */
object RegisterNodeSerializer extends Serializer[RegisterNode] {

  override def write(kryo: Kryo, output: Output, registerNode: RegisterNode): Unit = {
    output.writeInt(registerNode.nodeId.id)
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[RegisterNode]): RegisterNode = {
    RegisterNode(NodeId(input.readInt()))
  }
}
