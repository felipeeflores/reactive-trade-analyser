package analyser

import analyser.model._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest._

import cats.implicits._

class RawStockMarketDataParserSpec extends FunSpec with Matchers with TypeCheckedTripleEquals with EitherValues {

  describe("StockMarketDataParser") {

    it("should parse valid csv") {
      val validCsv = Seq(
        "1AG,20180102,0.031,0.032,0.031,0.032,100000",
        "3DP,20180102,0.145,0.145,0.14,0.14,840117"
      ).mkString("\n")

      val expected = Vector(
        StockMarketEntry(ticker = Ticker("1AG"), date = Datestamp("20180102"), open = Price(0.031), close = Price(0.032)),
        StockMarketEntry(ticker = Ticker("3DP"), date = Datestamp("20180102"), open = Price(0.145), close = Price(0.14))
      ).map(Right(_))

      val result = RawStockMarketDataParser.parse(RawStockMarketData("good-file", validCsv))

      result should === (expected)
    }

    it("should report errors") {
      val brokenCsv = Seq(
        "1AG,20180102,xxx,0.032,0.031,0.032,100000",
        "3DP,20180102,0.145,0.145,0.14"
      ).mkString("\n")

      val result = RawStockMarketDataParser.parse(RawStockMarketData("bad-file", brokenCsv))

      result.foreach(println)
      val (errors, _) = result.separate

      errors.length should === (2)
    }
  }

}
