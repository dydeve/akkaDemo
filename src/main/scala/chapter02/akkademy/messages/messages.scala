package chapter02.akkademy.messages

/**
  * scala的case class是可以序列化的。
  * 消息应该始终不可变
  *
  * 消息在网络应用程序上传输
  */
case class SetRequest(key: String, value: String)

case class SetIfNotExists(key: String, value: String)

case class GetRequest(key: String)

case class Delete(key: String)

case class KeyNotFoundException(key: String) extends Exception
