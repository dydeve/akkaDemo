### 5. 纵向扩展
纵向扩展指的是尽可能地利用单台机器上的硬件资源。
Akka 中提供了两种可以用来进行`多核并行`编程的抽象:`Future` 和 `Actor`
#### 5.4 并行编程
##### 5.4.1 使用Future并行编程
##### 5.4.2 使用actor并行编程
###### Router介绍
Router是一个用于`负载均衡`和`路由`的抽象。创建 Router 时，必须要传入一个 `Actor Group`，或者由 Router 来创建一个 `Actor Pool`。

有两种用来创建Router背后的Actor集合的机制。一种是由Router来创建这些Actor(一个Pool)， 另一种是把一个 Actor 列表(Group)传递给 Router。

当 Router 接收到消息时，就会将消息传递给 Group/Pool 中 的一个或多个 Actor。有多种策略可以用来决定 Router 选择下一个消息发送对象的顺序。 

- Actor运行在本地，我们需要一个包含多个Actor的Router 来支持使用多个CPU核进行并行运算
- 如果Actor运行在远程机器上，也可以使用Router 在服务器集群上分发工作 

如下图所示
![akka-router](../../resources/chapter05/akka-router.jpg)

###### 路由逻辑

详见 `akka.routing.Group` 的子类：
![akka-router-group](../../resources/chapter05/akka-router-group.jpg)

和 `akka.routing.Pool` 的子类：

![akka-router-pool](../../resources/chapter05/akka-router-pool.jpg)

| 路由策略           | 功能                                                         |
| :----------------- | :----------------------------------------------------------- |
| Round Robin        | 依次向 Pool/Group 中的各个节点发送消息，循环往复。Random——随机向各个节点发送消息 |
| Smallest Mailbox   | 向当前包含消息数量最少的 Actor 发送消息。由于远程 Actor 的邮箱大小未知，因此假设它们的队列中已经有消息在排队。所以会优先将消息发送给空闲的本地 Actor。 |
| Scatter Gather     | 向 Group/Pool 中的所有 Actor 都发送消息，使用接收到的第一个响应，丢弃之后收到的任何其他响应。如果需要确保能够尽快收到一个响应，那么可以 使用 scatter/gather。 |
| Tail Chopping      | 和 Scatter/Gather 类似，但是 Router 并不是一次性向 Group/Pool 中的所有 Actor 都发送一条消息，而是每向一个 Actor 发送消息后等待一小段时间。有着和 Scatter/Gather 类似的优点，但是相较而言有可能可以减少网络负载。 |
| Consistent Hashing | 给 Router 提供一个 key，Router 根据这个 key 生成哈希值。使用这个哈希值 来决定给哪个节点发送数据。想要将特定的数据发送到特定的目标位置时， 就可以使用哈希。在下章中，我们将讨论更多有关一致性哈希的问题。 |
| BalancingPool      | BalancingPool 这个路由策略有点特殊。只可以用于本地 Actor。多个 Actor 共享同一个邮箱，一有空闲就处理邮箱中的任务。这种策略可以确保所有 Actor 都处于繁忙状态。对于本地集群来说，经常会优先选择这个路由策略。 |

我们也可以实现自己的路由策略 

###### 向同一个 Router Group/Pool 中的所有 Actor 发送消息 

```scala
router ! akka.routing.Broadcast(msg)
```

###### 监督 Router Pool 中的路由对象 

如果使用 Pool 的方式创建 Router，由 Router 负责创建 Actor，那么这些路由对象会 成为 Router 的子节点。创建 Router 时，可以给 Router 提供一个自定义的监督策略。

使用 Group 方法创建 Router 的时候传入了事先已经存在的 Actor，所以没有办 法用 Router 来监督 Group 中的 Actor。


#### 5.5 使用 Dispatcher

##### 5.5.1 Dispatcher 解析 

Dispatcher 将如何执行任务与何时运行任务两者解耦。一般来说，Dispatcher 会包含一 些线程，这些线程会负责`调度并运行任务`，比如处理 Actor 的消息以及线程中的 Future 事件。 Dispatcher 是 Akka 能够支持`响应式编程`的关键，是负责完成任务的机制。 

所有 `Actor 或 Future `的工作都是由` Executor/Dispatcher `分配的资源来完成的 .

![akka-dispatcher](../../resources/chapter05/akka-dispatcher.jpg)

```scala
system.dispatcher //actor system's dispatcher
system.dispatchers.lookup("my-dispatcher"); //custom dispatcher
```
由于我们能够创建并获取这些基于 Executor 的 Dispatcher，因此可以使用它们来定义 `ThreadPool/ForkJoinPool` 来隔离运行任务的环境。

##### 5.5.2 Executor

Dispatcher 基于 Executor，所以在具体介绍 Dispatcher 之前，我们将介绍两种主要的 Executor 类型:`ForkJoinPool` 和 `ThreadPool`。

ThreadPool Executor有一个`工作队列`，队列中包含了要分配给各线程的工作。线程 空闲时就会从队列中认领工作。由于线程资源的创建和销毁开销很大，而 ThreadPool 允许线程的重用，所以就可以`减少创建和销毁线程的次数`，提高效率。

ForkJoinPool Executor 使用一种`分治算法`，递归地将任务分割成更小的子任务，然后 把子任务分配给不同的线程运行。接着再把运行结果组合起来。由于提交的任务不一定都能够被递归地分割成 ForkJoinTask，所以 ForkJoinPool Executor 有一个`工作窃取算法`， 允许空闲的线程“窃取”分配给另一个线程的工作。由于工作可能无法平均分配并完成， 所以工作窃取算法能够`更高效地利用硬件资源`。

ForkJoinPool Executor几乎总是比ThreadPool的Executor效率更高，是我们的默认选择。


