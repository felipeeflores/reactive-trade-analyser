package analyser

import analyser.model._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FunSpec, Matchers, OptionValues}

class IndicatorsSpec extends FunSpec with Matchers with TypeCheckedTripleEquals with OptionValues {

  describe("computeSMA") {

    it("should compute a 10SMA") {
      val indicator = Indicators.computeSimpleMovingAverage(Sma10, generateStockMarketEntries(Sma10.periodSpan))
      indicator.value should ===(Indicator(Sma10, IndicatorValue(5.5)))
    }

    it("should compute a 50SMA") {
      val indicator = Indicators.computeSimpleMovingAverage(Sma50, generateStockMarketEntries(Sma50.periodSpan))
      indicator.value should ===(Indicator(Sma50, IndicatorValue(25.5)))
    }
  }

  def generateStockMarketEntries(length: Int) = {
    Iterator.range(1, length + 1).toVector.map(number =>
      StockMarketEntry(
        ticker = Ticker("Ticker"),
        date = Datestamp("20180206"),
        open = Price(number.toDouble),
        close = Price(number.toDouble)
      )
    )
  }
}
