package service

import model.Position
import org.joda.time.DateTime
import org.specs2.Specification
import org.specs2.mock.Mockito

class CsvDataReaderSpec extends Specification with Mockito {
  def is =
    s2"""

    This is a specification to check the resilience of CsvDataReader towards wrongly formatted string

    The 'transform' method should
      return None if input is not a comma separated string $e1
      return None if input is comma separated but with more than 5 elements $e2
      return None if input is comma separated but with less than 5 elements $e3
      return Some(Position(None,None,None,None,String)) if first 4 elements are of illegal format $e4
      return Some(Position(Some,Some,Some,Some,String)) if all elements are of legal format $e5
    """

  val csvReader = new CsvDataReader(anyString)

  def e1 = csvReader.transform("A|B|C|D|E") must beNone

  def e2 = csvReader.transform("A,B,C,D,E,F") must beNone

  def e3 = csvReader.transform("A,B,C,D") must beNone

  def e4 = csvReader.transform("A,B,C,D,E") must beNone

  def e5 = csvReader.transform("2014-07-19T16:00:06.071Z,103.79211,71.50419417988532,1,600dfbe2") must
    beSome(Position(new DateTime("2014-07-19T16:00:06.071Z"), 103.79211F, 71.50419417988532, 1, "600dfbe2"))

}
