package service

import model.Position
import org.specs2.Specification

class ActualDataAccumulatorSpec extends Specification {
  def is =
    s2"""
        This is to check if the DataAccumulator builds the positionByFloor maps correctly

        The 'accumulate' method should end up with subject1 map with 2 entries (1 element in each list) and subject2 with 1 entry (2 element in the list) $e1

      """

  val accumulator = new ActualDataAccumulator
  val position1 = Position("2014-07-19T16:00:06.071Z,103.79211,71.50419417988532,1,600dfbe2").get
  val position2 = Position("2014-07-19T16:00:06.074Z,110.33613,100.6828393188978,1,5e7b40e1").get
  val position3 = Position("2014-07-19T16:00:06.076Z,110.066315,86.48873585227504,1,5e7b40e1").get
  val position4 = Position("2014-07-19T16:00:06.076Z,103.78499,71.45633073293511,2,600dfbe2").get
  val subject1 = "600dfbe2"
  val subject2 = "5e7b40e1"
  val it = Iterator(position1, position2, position3, position4)

  def e1 = {
    val positionByFloorTup = accumulator.accumulate(it,subject1,subject2)
    positionByFloorTup._1.keys should have size 2
    positionByFloorTup._1.get(1.toByte).get should have size 1
    positionByFloorTup._1.get(2.toByte).get should have size 1
    positionByFloorTup._2.keys should have size 1
    positionByFloorTup._2.get(1.toByte).get should have size 2
  }
}
