package service

import model.Position
import org.joda.time.DateTime

import scala.util.Try

sealed trait DataReader[A] {
  def source: String

  def header: Boolean

  def read: Iterator[A] = if (header) {
    io.Source.fromResource(source).getLines().drop(1).map(transform _).flatten
  } else {
    io.Source.fromResource(source).getLines().map(transform _).flatten
  }

  def transform(string: String): Option[A]
}

final case class CsvDataReader(source: String, header: Boolean = true) extends DataReader[Position] {
  def transform(string: String): Option[Position] = Position(string)
}
