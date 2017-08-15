package service

import model.{IntervalXY, OutputObject, Position}
import org.joda.time.{DateTime, Interval}

import scala.collection.immutable.Iterable

trait DataProcessor {
  type Floor = Byte
  type FloorPositions = Map[Floor, List[Position]]
  type CommonFloorPositions = Map[Floor, (List[Position], List[Position])]
  type Pathway = List[IntervalXY]
  def emptyCommonFloorPositions:CommonFloorPositions = Map()
  def emptyPathway: Pathway = List()
  def emptyFloorPositions:FloorPositions = Map()
}

case class ActualDataProcessor(data: (Map[Byte, List[Position]], Map[Byte, List[Position]])) extends DataProcessor {

  def meetingTimeAndPosition:Iterable[OutputObject] = {
    overlapOnFloor.flatMap {
      case (floor, subjectsData) =>
        val subjectsIntervalXY @ (subject1IntervalXY, subject2IntervalXY) = createIntervals(subjectsData)
        val interval = findOverlappingInterval(subjectsIntervalXY)
        (interval.getStartMillis to interval.getEndMillis by 1000).foldRight(List.empty[OutputObject]) {
          (timestamp, outputObjects) =>
            val positionOfSubject1AtThisTime = positionDuring(timestamp, subject1IntervalXY)
            val positionOfSubject2AtThisTime = positionDuring(timestamp, subject2IntervalXY)
            if (withinXMeter(1,
              positionOfSubject1AtThisTime._1, positionOfSubject1AtThisTime._2,
              positionOfSubject2AtThisTime._1, positionOfSubject2AtThisTime._2))
                outputObjects :+ OutputObject(floor, timestamp, positionOfSubject1AtThisTime._1, positionOfSubject1AtThisTime._2
                  , positionOfSubject2AtThisTime._1, positionOfSubject2AtThisTime._2)
            else
              outputObjects
        }
    }
  }

  def withinXMeter(x:Double, subject1X:Double, subject1Y:Double, subject2X:Double, subject2Y:Double): Boolean =
    math.abs(subject1X - subject2X) < x && math.abs(subject1Y - subject2Y) < x

  def overlapOnFloor: CommonFloorPositions = {
    val (subject1Data, subject2Data) = data
    val commonFloor = subject1Data.keys.toSet.intersect(subject2Data.keys.toSet)
    commonFloor.foldRight(emptyCommonFloorPositions) {
      (key, map) =>
        map + (key -> (subject1Data.getOrElse(key, List.empty), subject2Data.getOrElse(key, List.empty)))
    }
  }

  def createIntervals(input: (List[Position], List[Position])): (Pathway, Pathway) = {
    val (subject1Data, subject2Data) = input
    (convertToPathway(subject1Data), convertToPathway(subject2Data))
  }

  def convertToPathway(input: List[Position]): Pathway = {
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
    val x = input.sortBy(_.timestamp).sliding(2)
    x.foldLeft(emptyPathway) {
      (acc, window) =>
        val start = window(0)
        val end = window(1)
        val interval = new Interval(start.timestamp.getMillis, end.timestamp.getMillis)
        acc :+ IntervalXY(interval, start.x, start.y, end.x, end.y)
    }
  }

  def findOverlappingInterval(subjectsPathways:(Pathway, Pathway)):Interval = {
    val (subject1Data,subject2Data) = subjectsPathways
    val subject1Interval = new Interval(subject1Data.head.interval.getStartMillis, subject1Data.last.interval.getEndMillis)
    val subject2Interval = new Interval(subject2Data.head.interval.getStartMillis, subject2Data.last.interval.getEndMillis)
    subject1Interval.overlap(subject2Interval)
  }

  def positionDuring(epochWhen:Long, intervalXYList:List[IntervalXY]):(Double, Double) = {
    val intervalXYOpt = intervalXYList.find(intervalXY => {
      //i want to include the end point as well, joda excludes it by default
      val intervalEnd:Long = intervalXY.interval.getEndMillis + 1
      val intervalStart:Long = intervalXY.interval.getStartMillis
      val newInterval = new Interval(intervalStart, intervalEnd)
      newInterval.contains(epochWhen)
    })
    assert(intervalXYOpt.isDefined, "IntervalXY at this stage shouldn't be None")
    val intervalXY = intervalXYOpt.get
    val xInterpolated = interpolate(epochWhen, intervalXY.interval.getStartMillis, intervalXY.interval.getEndMillis, intervalXY.startX, intervalXY.endX)
    val yInterpolated = interpolate(epochWhen, intervalXY.interval.getStartMillis, intervalXY.interval.getEndMillis, intervalXY.startY, intervalXY.endY)
    (xInterpolated, yInterpolated)
  }

  def interpolate(epochWhen:Long, epochStart:Long, epochEnd:Long, position1:Double, position2:Double): Double = {
    assert(epochWhen <= epochEnd && epochWhen >= epochStart, "epochWhen should be between epoch1 and epoch2")
    position1+(epochWhen-epochStart)*((position2-position1)/(epochEnd-epochStart))
  }

}
