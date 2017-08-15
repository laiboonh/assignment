package service

import model.Position

trait DataAccumulator[A] {
  type Floor = Byte
  type FloorPositions = Map[Floor, List[Position]]
  def emptyFloorPositions:FloorPositions = Map()
  def accumulate(iterator: Iterator[A], subject1Uid: String, subject2Uid: String): (FloorPositions, FloorPositions)
}

class ActualDataAccumulator extends DataAccumulator[Position] {
  override def accumulate(iterator: Iterator[Position], subject1Uid: String, subject2Uid: String): (FloorPositions, FloorPositions) = {
    iterator.foldRight((emptyFloorPositions, emptyFloorPositions)){ (position, accumulator) =>
      position match {
        case Position(_, _, _, floor, `subject1Uid`) =>
          val oldMap = accumulator._1
          val newList:List[Position] = oldMap.getOrElse(floor,List.empty[Position]) :+ position
          val newMap = oldMap + (floor -> newList)
          (newMap, accumulator._2)
        case Position(_, _, _, floor, `subject2Uid`) =>
          val newList:List[Position] = accumulator._2.getOrElse(floor,List.empty[Position]) :+ position
          val newMap = accumulator._2 + (floor -> newList)
          (accumulator._1, newMap)
        case _ => accumulator
      }
    }
  }
}
