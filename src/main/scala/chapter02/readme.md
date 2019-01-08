### 2 actor与并发
`akka`也被称作是一个响应式平台
#### 2.1 响应式系统设计
#### 2.2 响应式4准则(设计目标)
##### 2.2.1.灵敏性
尽快对请求作出响应；如果可能出现错误，应该立即返回，将问题通知用户，不要让用户等待直到超时。
##### 2.2.2. 伸缩性
应用程序应该能够根据不同的工作负载进行伸缩扩展(尤其是通过增加计算资源来，进行扩展)。为了提供伸缩性，系统应该努力消除瓶颈。应当使吞吐量几乎线性增长
##### 2.2.3. 容错性
应该考虑到错误发生的情况，并且从容地对错误情况做出响应。
如果系统 的某个组件发生错误，对与该组件无关的请求不应该产生任何影响。
错误是难以避免的， 因此应该将错误造成的影响限制在发生错误的组件之内。
如果可能的话，通过`对重要组件及数据的备份和冗余`，
这些组件发生错误时不应该对其外部行为有任何影响。
##### 2.2.4. 事件驱动/消息驱动
使用`消息`而不直接进行`方法调用`提供了一种帮助我们满足另外 3 个响应式准则的方法。
消息驱动的系统着重于控制何时、何地以及如何对请求做出响应，允许做出响应的组件进行`路由`以及`负载均衡`。

由于`异步的消息驱动系统`只在真正需要时才会消耗资源(比如线程)，因此它对`系统资源的利用更为高效`。
消息也可以被发送到远程机器(`位置透明`)。因为要发送的消息暂存在Actor外的队列中，
并从该队列中发出，所以就能够通过`监督机制`使得发生错误的系统进行`自我恢复`。
##### 2.2.5 响应式准则的相关性

#### 2.3 剖析actor

#### 2.4 创建actor
我们从来都不会得到Actor的实例，从不调用Actor的方法，也不直接改变Actor的状态，`只向Actor发送消息`。
除此之外，我们也不会直接访问Actor的成员，而是通过消息传递来请求获取关于Actor状态的信息。使用消息传递代替直接方法调用可以加强封装性

通过使用基于消息的方法，我们可以相当完整地将Actor的实例封装起来。如果只通过消息进行相互通信的话，那么永远都不会需要获取Actor的实例。我们只需要一种机制来支持`向Actor发送消息并接收响应`。

在Akka中，这个指向Actor实例的引用叫做`ActorRef`。ActorRef是一个无类型的引用，将其指向的Actor封装起来，提供了更高层的抽象，并且给用户提供了一种与Actor进行通信的机制

指向actor的引用：
1. ActorRef
2. ActorSelection
```scala
//ActorRef.path 来查看Actor路径
ActorSelection selection = system.actorSelection("akka.tcp:// actorSystem@host.jason-goodwin.com:5678/user/KeanuReeves")
//进行网络间通信非常方便，位置透明
```

#### 2.5 Promise、Future 和事件驱动的编程模型
##### 2.5.1 阻塞与事件驱动 API
使用多线程来处理`阻塞式IO`时会遇到一些问题:
- 代码没有在返回类型中明确表示错误;
- 代码没有在返回类型中明确表示延时;
- 阻塞模型的吞吐量受到线程池大小的限制;
- 创建并使用许多线程会耗费额外的时间用于上下文切换，影响系统性能

非阻塞、异步的消息驱动系统可以`只运行少量的线程，并且不阻塞这些线程，只在需要计算资ExecutionContext源时才使用它们`。
这大大提高了系统的响应速度，并且能够更高效地利用系统资源。取决于具体实现的不同，异步系统还可以在返回类型中清晰地定义错误和延时等信息

##### 2.5.2 使用 Future 进行响应的 Actor
引入`import akka.pattern.ask`，开启隐式转换，`ActorRef`可以调用`?`
```scala
/**
   * Import this implicit conversion to gain `?` and `ask` methods on
   * [[akka.actor.ActorRef]], which will defer to the
   * `ask(actorRef, askSender => message)(timeout)` method defined here.
   *
   * {{{
   * import akka.pattern.ask
   *
   * // same as `ask(actor, askSender => Request(askSender))`
   * val future = actor ? { askSender => Request(askSender) }
   *
   * // same as `ask(actor, Request(_))`
   * val future = actor ? (Request(_))
   *
   * // same as `ask(actor, Request(_))(timeout)`
   * val future = actor ? (Request(_))(timeout)
   * }}}
   *
   * All of the above use a required implicit [[akka.util.Timeout]] and optional implicit
   * sender [[akka.actor.ActorRef]].
   */
  implicit def ask(actorRef: ActorRef): ExplicitlyAskableActorRef = new ExplicitlyAskableActorRef(actorRef)
```
##### 2.5.3 理解`Future`和`Promise`
##### 2.5.4 失败情况下执行代码
##### 2.5.5 失败中恢复
##### 2.5.6 异步地从失败中恢复
##### 2.5.7 链式操作
##### 2.5.8 组合future
##### 2.5.9 处理future列表
##### 2.5.10 future速查表
header 1 | header 2 | java CompletableFuture
---|---|---
Transform Value  | .map(x => y)  | .thenApply(x -> y) 
Transform Value Async  | .flatMap(x => futureOfY) |.thenCompose(x -> futureOfY)    
Return Value if Error  | .recover(t => y   |.exceptionally(t -> y) 
Return Value Async if Error  | .recoverWith(t => futureOfY)  |.handle(t,x -> futureOfY).thenCompose(x->x)  

