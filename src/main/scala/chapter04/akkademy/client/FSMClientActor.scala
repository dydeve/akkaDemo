package chapter04.akkademy.client

import akka.actor.FSM
import chapter04.akkademy.client.StateContainerTypes.RequestQueue
import chapter04.akkademy.{Connected, Request}

//sealed: 不能在类定义的文件之外定义任何新的子类,case如果少处理了,编译时会提示
sealed trait State

case object DisConnected extends State

case object Connected extends State

case object ConnectedAndPending extends State

case object Flush

case object connectedMsg

object StateContainerTypes {
  type RequestQueue = List[Request]
}

class FSMClientActor(address: String) extends FSM[State, RequestQueue] {

  private val remoteDb = context.system.actorSelection(address)

  //起始状态
  startWith(DisConnected, List.empty[Request])

  when(DisConnected) {
    case Event(_: Connected, container: RequestQueue) => //If we get back a ping from db, change state
      //默认就会转在线
      if (container.headOption.isEmpty) {
        //转为在线
        goto(Connected)
      } else {
        goto(ConnectedAndPending)
      }

    case Event(x: Request, container: RequestQueue) =>
      remoteDb ! new Connected //Ping remote db to see if we're connected if not yet marked online.
      stay().using(container :+ x) //Stash the msg
    //stay using container:+x
    case x =>
      log.info("uhh didn't quite get that:{}", x)
      stay()
  }

  when(Connected) {
    case Event(x: Request, container: RequestQueue) =>
      //1.转在线+有内容状态
      //2.添加到队列中
      goto(ConnectedAndPending) using container :+ x
  }

  when(ConnectedAndPending) {
    case Event(Flush, container) =>
      //接受Flush后,会发送 List请求,包括了get和pset
      remoteDb ! container
      //发送完内容,再次转为离线,并且将容器内容清空
      goto(Connected) using Nil
    case Event(x: Request, container: RequestQueue) =>
      //保持ConnectedAndPending状态
      stay using container :+ x
  }

  //开始启动
  initialize()
}
