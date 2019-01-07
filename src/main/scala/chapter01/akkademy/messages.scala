package chapter01.akkademy

/**
  * 消息必须永远是不可变的，这样可以确保我们和我们的团队不通过多个执行上下文/ 线程来做一些不安全的操作，
  * 从而避免一些奇怪而又出人意料的行为。
  *
  * 同样要记住这些 消息除了会发送给本地的 Actor 以外，也可能会发送给另一台机器上的 Actor。
  * 如果可能的话，把所有值都定义为 val(Scala)或 final(Java)，并且使用不可变集合及类型,
  * 比如 Google Guava(Java)和 Scala 标准库所提供的集合及类型。
  */
case class SetRequest(key: String, value: Any)

case class GetRequest(key: String)