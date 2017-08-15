package service

import model.Position

sealed trait DataReader[A] {
  def source: String

  def header: Boolean

  def read: Iterator[A] = if (header) {
    io.Source.fromResource(source).getLines().drop(1).map(process).flatten
  } else {
    io.Source.fromResource(source).getLines().map(process).flatten
  }

  def process(string: String): Option[A]
}

final case class CsvDataReader(source: String, header: Boolean = true) extends DataReader[Position] {
  def process(string: String): Option[Position] = Position(string)
}
