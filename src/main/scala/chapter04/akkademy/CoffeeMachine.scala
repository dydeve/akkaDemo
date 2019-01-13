package chapter04.akkademy

import akka.actor.FSM
import chapter04.akkademy.CoffeeMachine._
import chapter04.akkademy.CoffeeProtocol._
import org.slf4j.LoggerFactory

/**
  * @Description:
  * @Date 下午2:24 2019/1/13
  * @Author: joker
  */
object CoffeeMachine {

  sealed trait MachineState

  case object Open extends MachineState

  case object ReadyToBuy extends MachineState

  case object PoweredOff extends MachineState

  //（已放入的钱， 咖啡钱， 咖啡可售量）
  case class MachineData(currentTxTotal: Int, costOfCoffee: Int, coffeesLeft: Int)

}

object CoffeeProtocol {

  trait UserInteraction

  trait VendorInteraction

  //放钱
  case class Deposit(value: Int) extends UserInteraction

  case class Balance(value: Int) extends UserInteraction

  case object Cancel extends UserInteraction

  //煮咖啡
  case object BrewCoffee extends UserInteraction

  case object GetCostOfCoffee extends UserInteraction

  case object ShutDownMachine extends VendorInteraction

  case object StartUpMachine extends VendorInteraction


  case class SetNumberOfCoffee(quantity: Int) extends VendorInteraction

  case class SetCostOfCoffee(price: Int) extends VendorInteraction

  case object GetNumberOfCoffee extends VendorInteraction

  case class MachineError(errorMsg: String)

}

class CoffeeMachine extends FSM[MachineState, MachineData] {
  private val logger = LoggerFactory.getLogger(classOf[CoffeeMachine])

  startWith(Open, MachineData(currentTxTotal = 0, costOfCoffee = 5, coffeesLeft = 10))

  when(Open) {
    case Event(_, MachineData(_, _, coffeesLeft)) if (coffeesLeft <= 0) => {
      logger.warn("No more coffee")
      sender ! MachineError("No more coffee")
      goto(PoweredOff)
    }
    case Event(Deposit(value), MachineData(currentTxTotal, costOfCoffee, coffeesLeft)) if (value + currentTxTotal) >= stateData.costOfCoffee => {
      goto(ReadyToBuy) using stateData.copy(currentTxTotal = currentTxTotal + value)
      //todo self ! ReadyToBuy 付完款应该直接煮咖啡
    }
    //If the total deposit is less than than the price of the coffee, then stay in the current state with the current deposit amount incremented.
    case Event(Deposit(value), MachineData(currentTxTotal, costOfCoffee, coffeesLeft)) if (value + currentTxTotal) < stateData.costOfCoffee => {
      val cumulativeValue = currentTxTotal + value
      logger.debug(s"staying at open with currentTxTotal $cumulativeValue")
      stay using stateData.copy(currentTxTotal = cumulativeValue)
    }
    case Event(SetNumberOfCoffee(quantity), _) =>
      stay using stateData.copy(coffeesLeft = quantity)
    case Event(GetNumberOfCoffee, _) =>
      sender ! (stateData.coffeesLeft)
      stay()
    case Event(SetCostOfCoffee(price), _) =>
      stay using stateData.copy(costOfCoffee = price)
    case Event(GetCostOfCoffee, _) =>
      sender ! (stateData.costOfCoffee)
      stay()
  }

  //Ignoring the case when user deposits cash during `ReadyToBuy` state
  when(ReadyToBuy) {
    case Event(BrewCoffee, MachineData(currentTxTotal, costOfCoffee, coffeesLeft)) => {
      val balanceToBeDispensed = currentTxTotal - costOfCoffee
      logger.debug(s"Balance is $balanceToBeDispensed")
      if (balanceToBeDispensed > 0) {
        sender ! Balance(value = balanceToBeDispensed)
        //goto(Open) using stateData.copy(currentTxTotal = 0, coffeesLeft = coffeesLeft - 1)
      }
      //else goto(Open) using stateData.copy(currentTxTotal = 0, coffeesLeft = coffeesLeft - 1)
      goto(Open) using stateData.copy(currentTxTotal = 0, coffeesLeft = coffeesLeft - 1)
    }
  }

  when(PoweredOff) {
    case (Event(StartUpMachine, _)) => goto(Open)
    case _ => {
      logger.warn("Machine Powered down.  Please start machine first with StartUpMachine")
      sender ! MachineError("Machine Powered down.  Please start machine first with StartUpMachine")
      stay()
    }
  }

  whenUnhandled {
    case Event(ShutDownMachine, MachineData(currentTxTotal, costOfCoffee, coffeesLeft)) => {
      sender ! Balance(value = currentTxTotal)
      goto(PoweredOff) using stateData.copy(currentTxTotal = 0)
    }
    case Event(Cancel, MachineData(currentTxTotal, _, _)) => {
      logger.debug(s"Balance is $currentTxTotal")
      sender ! Balance(value = currentTxTotal)
      goto(Open) using stateData.copy(currentTxTotal = 0)
    }
  }

  onTransition {
    case Open -> ReadyToBuy => logger.debug("From Transacting to ReadyToBuy")
    case ReadyToBuy -> Open => logger.debug("From ReadyToBuy to Open")
  }

}