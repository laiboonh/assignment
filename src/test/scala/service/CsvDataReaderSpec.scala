package service

import model.Position
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}

class CsvDataReaderSpec extends WordSpec with Matchers {

  private val csvReader = new CsvDataReader("")

  "A CsvDataReader" when {
    "processing a non comma separated string" should {
      "return None" in {
        csvReader.process("A|B|C|D|E") should be (None)
      }
    }
    "processing a comma separated string with more than 5 elements" should {
      "return None" in {
        csvReader.process("A,B,C,D,E,F") should be (None)
      }
    }
    "processing a comma separated string with less than 5 elements" should {
      "return None" in {
        csvReader.process("A,B,C,D") should be (None)
      }
    }
    "processing a comma separated string if the first4 elemetns are of illegal format" should {
      "return None" in {
        csvReader.process("A,B,C,D,E") should be (None)
      }
    }
    "processing a comma separated string of legally formatted elements" should {
      "return Some[Position]" in {
        csvReader.process("2014-07-19T16:00:06.071Z,103.79211,71.50419417988532,1,600dfbe2") should be
          (Some(Position(new DateTime("2014-07-19T16:00:06.071Z"), 103.79211F, 71.50419417988532, 1, "600dfbe2")))
      }
    }
  }
}
