package service

import model.{IntervalXY, Position}
import org.joda.time.{DateTime, Interval}
import org.specs2.Specification

class ActualDataProcessorSpec extends Specification {
  def is =
    s2"""
        This is to check if the DataProcessor processes the data correctly
        The 'overlapOnFloor' method should find out the common floor between 2 subjects and keep the subjects data in a map $e1
        The 'convertToIntervalXY' method should take a list of position and convert them the a list of IntervalXYs $e2
        The 'findOverlappingInterval' method should assumes inputs are already sorted and find overlapping interval between the two inputs $e3
        The 'interpolate' method should return a value for a certain time given start and end time and start and end values $e4
        The 'positionDuring' method should be able to return an interpolated coordinate for a given time given a list of IntervalXY $e5
      """

  val position1 = Position("2014-07-19T16:00:06.071Z,103.79211,71.50419417988532,1,600dfbe2").get
  val position2 = Position("2014-07-19T16:00:06.074Z,110.33613,100.6828393188978,1,5e7b40e1").get
  val position3 = Position("2014-07-19T16:00:06.076Z,110.066315,86.48873585227504,1,5e7b40e1").get
  val position4 = Position("2014-07-19T16:00:06.076Z,103.78499,71.45633073293511,2,600dfbe2").get
  val position5 = Position("2014-07-19T16:00:06.070Z,110.066315,86.48873585227504,1,5e7b40e1").get


  val subject1Data = Map(1.toByte -> List(position1), 2.toByte -> List(position4))
  val subject2Data = Map(1.toByte -> List(position2, position3))
  val dataProcessor = new ActualDataProcessor((subject1Data, subject2Data))

  val utc2 = new DateTime("2014-07-19T16:00:06.074Z").getMillis
  val utc3 = new DateTime("2014-07-19T16:00:06.076Z").getMillis
  val utc5 = new DateTime("2014-07-19T16:00:06.070Z").getMillis

  def e1 = dataProcessor.overlapOnFloor === Map(1.toByte -> (List(position1), List(position2, position3)))

  def e2 = dataProcessor.convertToIntervalXY(List(position2, position3, position5)) ===
    List(
      IntervalXY(new Interval(utc5, utc2), position5.x, position5.y, position2.x, position2.y),
      IntervalXY(new Interval(utc2, utc3), position2.x, position2.y, position3.x, position3.y)
    )

  def e3 = {
    val input1 = List(
      IntervalXY(new Interval(utc5, utc2), position5.x, position5.y, position2.x, position2.y),
      IntervalXY(new Interval(utc2, utc3), position2.x, position2.y, position3.x, position3.y)
    )
    val input2 = List(
      IntervalXY(new Interval(utc2, utc3), position2.x, position2.y, position3.x, position3.y)
    )
    dataProcessor.findOverlappingInterval(input1,input2) === new Interval(utc2,utc3)
  }

  def e4 = {
    dataProcessor.interpolate(3,1,5,20,50) === 35.0
    dataProcessor.interpolate(1,1,5,20,50) === 20.0
    dataProcessor.interpolate(5,1,5,20,50) === 50.0
  }

  def e5 = {
    val intervalXYList = List(
      IntervalXY(new Interval(utc5, utc2), 20, 50, 30, 40),
      IntervalXY(new Interval(utc2, utc3), 30, 40, 10, 20)
    )
    dataProcessor.positionDuring(1405785606074L, intervalXYList) === (30.0,40.0)
    dataProcessor.positionDuring(1405785606076L, intervalXYList) === (10.0,20.0)
    dataProcessor.positionDuring(1405785606072L, intervalXYList) === (25.0,45.0)
  }

}
