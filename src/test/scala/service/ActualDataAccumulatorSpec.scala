package service

import model.Position
import org.scalatest.{Matchers, WordSpec}

class ActualDataAccumulatorSpec extends WordSpec with Matchers {

  private val accumulator = new ActualDataAccumulator
  private val position1 = Position("2014-07-19T16:00:06.071Z,103.79211,71.50419417988532,1,600dfbe2").get
  private val position2 = Position("2014-07-19T16:00:06.074Z,110.33613,100.6828393188978,1,5e7b40e1").get
  private val position3 = Position("2014-07-19T16:00:06.076Z,110.066315,86.48873585227504,1,5e7b40e1").get
  private val position4 = Position("2014-07-19T16:00:06.076Z,103.78499,71.45633073293511,2,600dfbe2").get
  private val subject1 = "600dfbe2"
  private val subject2 = "5e7b40e1"
  private val iterator = Iterator(position1, position2, position3, position4)

  "An ActualDataAccumulator" when {
    "running through a list of positions" should {
      "accumulate and build up a Map" in {
        val positionByFloorTup = accumulator.accumulate(iterator,subject1,subject2)
        positionByFloorTup._1.keys should have size 2
        positionByFloorTup._1.get(1.toByte).get should have size 1
        positionByFloorTup._1.get(2.toByte).get should have size 1
        positionByFloorTup._2.keys should have size 1
        positionByFloorTup._2.get(1.toByte).get should have size 2
      }
    }
  }
}
