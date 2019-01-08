package chapter02

import akka.actor.Status
import chapter02.akkademy.client.Client
import chapter02.akkademy.messages.KeyNotFoundException
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * @Description:
  * @Date 下午8:22 2019/1/8
  * @Author: joker
  */
class SClientIntegrationSpec extends FunSpec with Matchers {

  val client = new Client("127.0.0.1:2553")

  import scala.concurrent.ExecutionContext.Implicits.global

  def set(): Unit = {
    client.set("123", "123").onSuccess({
      case o => println(s"set success $o")
    })
    Thread.sleep(500)
  }

  def result(future: Future[Any]) = Await.result(future, 3 second)

  describe("akkademyDbClient") {
    it("set a value, get with right key") {
      set()

      val future = client.get("123")
      //val value = Await.result(future.mapTo[String], 3 second)
      val value = Await.result(future, 3 second)
      value should equal("123")

    }

    it("set a value, get with wrong key") {
      set()

      /**
        * 出异常 因为future没有recoverWith
      val future = client.get("no exists")
      future.recoverWith({
        case knfe: KeyNotFoundException =>
          println(s"key not found, e:$knfe")
          Future("ok")
      })
      */
      val future = client
        .get("no exists")
        .recoverWith({
          case knfe: KeyNotFoundException =>
            println(s"key not found, e:$knfe")
            Future("ok")
        })

      result(future) should equal("ok")
    }

    it("setIfNotExists") {
      val notExistFuture = client.setIfNotExists("321", "321")
      result(notExistFuture) should equal(None)

      val existFuture = client.setIfNotExists("321", null)
      result(existFuture) should equal("321")
    }

    it("get then delete then get") {
      client.set("321", "321").onSuccess({
        case o => println(s"set success $o")
      })
      Thread.sleep(500)

      result(client.get("321")) should equal("321")
      result(client.delete("321")) should equal(Status.Success)
      val future = client.get("321").recoverWith({
        case e: Exception => Future("fine")
      })

      result(future) should equal("fine")

    }

  }

}
