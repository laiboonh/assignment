package service

import model.{IntervalXY, Position}
import org.joda.time.{DateTime, Interval}
import org.scalatest.{Matchers, WordSpec}

class ActualDataProcessorSpec extends WordSpec with Matchers {

  private val position1 = Position("2014-07-19T16:00:06.071Z,103.79211,71.50419417988532,1,600dfbe2").get
  private val position2 = Position("2014-07-19T16:00:06.074Z,110.33613,100.6828393188978,1,5e7b40e1").get
  private val position3 = Position("2014-07-19T16:00:06.076Z,110.066315,86.48873585227504,1,5e7b40e1").get
  private val position4 = Position("2014-07-19T16:00:06.076Z,103.78499,71.45633073293511,2,600dfbe2").get
  private val position5 = Position("2014-07-19T16:00:06.070Z,110.066315,86.48873585227504,1,5e7b40e1").get

  private val subject1Data = Map(1.toByte -> List(position1), 2.toByte -> List(position4))
  private val subject2Data = Map(1.toByte -> List(position2, position3))
  private val dataProcessor = new ActualDataProcessor((subject1Data, subject2Data))

  private val utc2 = position2.timestamp.getMillis
  private val utc3 = position3.timestamp.getMillis
  private val utc5 = position5.timestamp.getMillis

  "overlapOnFloor" should {
      "return a map that indicates the common floor between 2 subjects and their respective positions on these floors" in {
        dataProcessor.overlapOnFloor === Map(1.toByte -> (List(position1), List(position2, position3)))
      }
  }

  "convertToIntervalXY" when {
    "given a list of positions" should {
      "convert them to a list of IntervalXYs depicting how the positions changed overtime" in {
        dataProcessor.convertToPathway(List(position2, position3, position5)) ===
          List(
            IntervalXY(new Interval(utc5, utc2), position5.x, position5.y, position2.x, position2.y),
            IntervalXY(new Interval(utc2, utc3), position2.x, position2.y, position3.x, position3.y)
          )
      }
    }
  }

  "findOverlappingInterval" when {
    "given a tuple consisting of 2 subjects \"pathway\" depicted by IntervalXYs " should {
      "return the interval in which the 2 pathways overlap in time" in {
        val input1 = List(
          IntervalXY(new Interval(utc5, utc2), position5.x, position5.y, position2.x, position2.y),
          IntervalXY(new Interval(utc2, utc3), position2.x, position2.y, position3.x, position3.y)
        )
        val input2 = List(
          IntervalXY(new Interval(utc2, utc3), position2.x, position2.y, position3.x, position3.y)
        )
        dataProcessor.findOverlappingInterval(input1,input2) === new Interval(utc2,utc3)
      }
    }
  }

  "interpolate" when {
    "given a time(epochWhen), start and end time start and end positions" should {
      "interpolate and return the position of subject during given time(epochWhen)" in {
        dataProcessor.interpolate(3,1,5,20,50) === 35.0
        dataProcessor.interpolate(1,1,5,20,50) === 20.0
        dataProcessor.interpolate(5,1,5,20,50) === 50.0
      }
    }
  }

  "positionDuring" when {
    "given a time(epochWhen) and a pathway" should {
      "return the position of the subject during that given time" in {
        val intervalXYList = List(
          IntervalXY(new Interval(utc5, utc2), 20, 50, 30, 40),
          IntervalXY(new Interval(utc2, utc3), 30, 40, 10, 20)
        )
        dataProcessor.positionDuring(1405785606074L, intervalXYList) === (30.0,40.0)
        dataProcessor.positionDuring(1405785606076L, intervalXYList) === (10.0,20.0)
        dataProcessor.positionDuring(1405785606072L, intervalXYList) === (25.0,45.0)
      }
    }
  }

}
