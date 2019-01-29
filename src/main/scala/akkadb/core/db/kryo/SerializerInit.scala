package akkadb.core.db.kryo

import com.esotericsoftware.kryo.Kryo
import org.slf4j.LoggerFactory

class SerializerInit {
  private val logger = LoggerFactory.getLogger(classOf[SerializerInit])
  def customize(kryo: Kryo): Unit = {
    logger.info("Initialized Kryo")

    // cluster
    kryo.register(classOf[akkadb.core.db.actor.protocol.RegisterNode], RegisterNodeSerializer, 50)

    // write -- request
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeWriteDataLocal], StorageNodeWriteDataLocalSerializer, 60)

    // write -- responses
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeFailedWrite],     StorageNodeWriteResponseSerializer, 70)
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeSuccessfulWrite], StorageNodeWriteResponseSerializer, 71)
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeConflictedWrite], StorageNodeWriteResponseSerializer, 72)

    // read - request
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeLocalRead], StorageNodeLocalReadSerializer, 80)

    // read - responses
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeFoundRead],      StorageNodeReadResponseSerializer, 90)
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeConflictedRead], StorageNodeReadResponseSerializer, 91)
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeNotFoundRead],   StorageNodeReadResponseSerializer, 92)
    kryo.register(classOf[akkadb.core.db.actor.protocol.StorageNodeFailedRead],     StorageNodeReadResponseSerializer, 93)

    ()
  }
}
