package analyser

import analyser.model._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FunSpec, Matchers}

class EnricherSpec extends FunSpec with Matchers with TypeCheckedTripleEquals {

  describe("Enricher") {
    val batches =  Vector(
      generateStockMarketEntries(Sma10.periodSpan, "ABC"),
      generateStockMarketEntries(Sma10.periodSpan, "XYZ")
    )
    val result = Enricher.enrichStockDataWithIndicator(Sma10, batches)

    it("should compute an entry per batch") {
      result.length should ===(2)
    }

    it("should compute the requested indicator") {
      result.forall(res => res.indicators.exists(_.indicatorType == Sma10)) should ===(true)
    }
  }

  def generateStockMarketEntries(length: Int, ticker: String): Vector[StockMarketEntry] = {
    Iterator.range(1, length + 1).toVector.map(number =>
      StockMarketEntry(
        ticker = Ticker(ticker),
        date = Datestamp("20180206"),
        open = Price(number.toDouble),
        close = Price(number.toDouble)
      )
    )
  }
}
