package service

import model.Position

trait DataAccumulator[A] {
  def accumulate(iterator: Iterator[A], subject1Uid: String, subject2Uid: String): Tuple2[Map[Byte, List[Position]], Map[Byte, List[Position]]]
}

class ActualDataAccumulator extends DataAccumulator[Position] {
  override def accumulate(iterator: Iterator[Position], subject1Uid: String, subject2Uid: String): (Map[Byte, List[Position]], Map[Byte, List[Position]]) = {
    iterator.foldRight((Map.empty[Byte, List[Position]], Map.empty[Byte, List[Position]])){ (position, accumulator) =>
      val floor = position.floor
      position.uid match {
        case `subject1Uid` =>
          val oldMap = accumulator._1
          val newList:List[Position] = oldMap.getOrElse(floor,List.empty[Position]) :+ position
          val newMap = oldMap + (floor -> newList)
          (newMap, accumulator._2)
        case `subject2Uid` =>
          val newList:List[Position] = accumulator._2.getOrElse(floor,List.empty[Position]) :+ position
          val newMap = accumulator._2 + (floor -> newList)
          (accumulator._1, newMap)
        case _ => accumulator
      }
    }
  }
}
