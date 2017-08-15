import model.{OutputObject, Position}
import org.joda.time.{DateTime, DateTimeZone}
import service.{ActualDataAccumulator, ActualDataProcessor, CsvDataReader}

import scala.collection.immutable.Iterable

object Main {

  DateTimeZone.setDefault(DateTimeZone.UTC)

  def main(args: Array[String]): Unit = {
    if(args.length!=2) throw new RuntimeException("Please provide 2 uids")

    val csvDataReader = new CsvDataReader("reduced.csv")
    val positionDataIterator = csvDataReader.read
    val dataAccumulator = new ActualDataAccumulator
    val subjectsPositionDataByFloor: (Map[Byte, List[Position]], Map[Byte, List[Position]]) = dataAccumulator.accumulate(positionDataIterator, args(0), args(1))
    val dataProcessor = new ActualDataProcessor(subjectsPositionDataByFloor)
    val result: Iterable[OutputObject] = dataProcessor.meetingTimeAndPosition

    result.foreach {
      res => println(s"${args(0)} and ${args(1)} meet on floor: ${res.floor}, time: ${new DateTime(res.time)}, position: (${res.subject1X}, ${res.subject1Y}) & position: (${res.subject2X}, ${res.subject2Y})")
    }

  }
}
