package model

import org.joda.time.DateTime

import scala.util.Try

case class Position(timestamp: DateTime, x:Float, y:Double, floor:Byte, uid:String)

case object Position {

  def apply(string:String):Option[Position] =
    string.split(',') match {
      case Array(timestamp, x, y, floor, uid) =>
        val timestampOpt = Try(new DateTime(timestamp)).toOption
        val xOpt = Try(x.toFloat).toOption
        val yOpt = Try(y.toDouble).toOption
        val floorOpt = Try(floor.toByte).toOption
        if (timestampOpt == None || xOpt == None || yOpt == None || floorOpt == None) None
        else Some(Position(timestampOpt.get, xOpt.get, yOpt.get, floorOpt.get, uid))
      case _ => None
    }
}
