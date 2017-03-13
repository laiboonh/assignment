package service

import model.{IntervalXY, Position}
import org.joda.time.{DateTime, Interval}

import scala.collection.immutable.Iterable

trait DataProcessor[A] {
  def data: Tuple2[Map[Byte, List[A]], Map[Byte, List[A]]]
}

case class ActualDataProcessor(data: Tuple2[Map[Byte, List[Position]], Map[Byte, List[Position]]]) extends DataProcessor[Position] {

  def meetingTimeAndPosition:Iterable[(Byte, Long, Double, Double, Double, Double)] = {
    overlapOnFloor.flatMap {
      entry =>
        val floor = entry._1
        val subjectsData:(List[Position],List[Position]) = entry._2
        val subjectsIntervalXY:(List[IntervalXY], List[IntervalXY]) = createIntervals(subjectsData)
        val interval:Interval = findOverlappingInterval(subjectsIntervalXY)
        (interval.getStartMillis to interval.getEndMillis by 1000).foldRight(List.empty[(Byte,Long,Double,Double,Double,Double)]) {
          (timestamp, accum) =>
            val positionOfSubject1AtThisTime = positionDuring(timestamp, subjectsIntervalXY._1)
            val positionOfSubject2AtThisTime = positionDuring(timestamp, subjectsIntervalXY._2)
            if (withinXMeter(1,
              positionOfSubject1AtThisTime._1, positionOfSubject1AtThisTime._2,
              positionOfSubject2AtThisTime._1, positionOfSubject2AtThisTime._2))
              accum :+ (floor, timestamp, positionOfSubject1AtThisTime._1, positionOfSubject1AtThisTime._2
                , positionOfSubject2AtThisTime._1, positionOfSubject2AtThisTime._2)
            else accum
        }
    }
  }

  def withinXMeter(x:Double, subject1X:Double, subject1Y:Double, subject2X:Double, subject2Y:Double): Boolean = {
    math.abs(subject1X - subject2X) < x && math.abs(subject1Y - subject2Y) < x
  }


  def overlapOnFloor: Map[Byte, Tuple2[List[Position], List[Position]]] = {
    val commonFloor = data._1.keys.toSet.intersect(data._2.keys.toSet)
    commonFloor.foldRight(Map.empty[Byte, Tuple2[List[Position], List[Position]]]) { (key, map) =>
      val subject1Data = data._1
      val subject2Data = data._2
      map + (key -> (subject1Data.getOrElse(key, List.empty), subject2Data.getOrElse(key, List.empty)))
    }
  }

  def createIntervals(input: Tuple2[List[Position], List[Position]]): Tuple2[List[IntervalXY], List[IntervalXY]] = {
    val subject1Data = input._1
    val subject2Data = input._2
    (convertToIntervalXY(subject1Data), convertToIntervalXY(subject2Data))
  }

  def convertToIntervalXY(input: List[Position]): List[IntervalXY] = {
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val x = input.sortBy(_.timestamp).sliding(2)
    x.foldLeft(List.empty[IntervalXY]) {
      (acc, window) =>
        val start = window(0)
        val end = window(1)
        val interval = new Interval(start.timestamp.getMillis, end.timestamp.getMillis)
        acc :+ IntervalXY(interval, start.x, start.y, end.x, end.y)
    }
  }

  def findOverlappingInterval(input:Tuple2[List[IntervalXY], List[IntervalXY]]):Interval = {
    val subject1Data = input._1
    val subject2Data = input._2
    val subject1Interval = new Interval(subject1Data.head.interval.getStartMillis, subject1Data.last.interval.getEndMillis)
    val subject2Interval = new Interval(subject2Data.head.interval.getStartMillis, subject2Data.last.interval.getEndMillis)
    subject1Interval.overlap(subject2Interval)
  }

  def positionDuring(epoc:Long, intervalXYList:List[IntervalXY]):Tuple2[Double,Double] = {
    val intervalXYOpt = intervalXYList.find(intervalXY => {
      //i want to include the end point as well, joda excludes it by default
      val intervalEnd:Long = intervalXY.interval.getEndMillis + 1
      val intervalStart:Long = intervalXY.interval.getStartMillis
      val newInterval = new Interval(intervalStart, intervalEnd)
      newInterval.contains(epoc)
    })
    assert(intervalXYOpt.isDefined, "IntervalXY at this stage shouldn't be None")
    val intervalXY = intervalXYOpt.get
    val xInterpolated = interpolate(epoc, intervalXY.interval.getStartMillis, intervalXY.interval.getEndMillis, intervalXY.startX, intervalXY.endX)
    val yInterpolated = interpolate(epoc, intervalXY.interval.getStartMillis, intervalXY.interval.getEndMillis, intervalXY.startY, intervalXY.endY)
    (xInterpolated, yInterpolated)
  }

  def interpolate(epochWhen:Long, epoch1:Long, epoch2:Long, value1:Double, value2:Double): Double = {
    assert(epochWhen <= epoch2 && epochWhen >= epoch1, "epochWhen should be between epoch1 and epoch2")
    value1+(epochWhen-epoch1)*((value2-value1)/(epoch2-epoch1))
  }

}
