package analyser

import analyser.model._
import cats.data.NonEmptyVector
import monix.eval.Task
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FunSpec, Matchers, OptionValues}

import scala.collection.mutable.ListBuffer
import scala.reflect.io.Path
import scala.util.Random

class StockTradeAnalyserSpec extends FunSpec with Matchers with TypeCheckedTripleEquals with OptionValues {

  describe("StockTradeAnalyser") {

    val ticker3dp = Ticker("3DP")
    val ticker3pl = Ticker("3PL")

    describe("10SMA indicator") {
      val testHelper = new StockTradeAnalyserSpecHelper(Sma10.periodSpan)
      testHelper.stockTradeAnalyser.analyse (
        config = Config(marketDataDir = "./foo"),
        tickers = NonEmptyVector(ticker3dp, Vector(ticker3pl)),
        indicators = Vector(Sma10),
        dataFrameSize = None
      )

      it("should analyse only specified tickers") {
        testHelper.dataBuffer.map(_.stockMarketEntry.ticker.value).distinct should contain only (ticker3dp.value, ticker3pl.value)
      }

      it("should compute expected number of moving averages") {
        val sma10Entries = testHelper.dataBuffer.filter(entry => entry.indicators.exists(i => i.indicatorType == Sma10))

        sma10Entries.length should ===(testHelper.dataBuffer.length)
        sma10Entries.length should ===(10 * 2) //ten entries per ticker
      }

      it("should compute 10SMA indicators") { // redundant test left out for illustration purposes
        val indicators = testHelper.dataBuffer.flatMap(_.indicators).toVector
        val expectedIndicatorValues = Iterator.range(5, 15).map(n => Indicator(Sma10, IndicatorValue(n + 0.5))).toVector
        //same indicators for both tickers
        indicators should contain only (expectedIndicatorValues: _*)
      }
    }


    describe("50SMA indicator") {
      val testHelper = new StockTradeAnalyserSpecHelper(Sma50.periodSpan)
      testHelper.stockTradeAnalyser.analyse (
        config = Config(marketDataDir = "./foo"),
        tickers = NonEmptyVector(ticker3dp, Vector(ticker3pl)),
        indicators = Vector(Sma50),
        dataFrameSize = None
      )

      it("should analyse only specified tickers") {
        testHelper.dataBuffer.map(_.stockMarketEntry.ticker.value).distinct should contain only (ticker3dp.value, ticker3pl.value)
      }

      it("should compute expected number of moving averages") {
        val sma50Entries = testHelper.dataBuffer.filter(entry => entry.indicators.exists(i => i.indicatorType == Sma50))

        sma50Entries.length should ===(testHelper.dataBuffer.length)
        sma50Entries.length should ===(50 * 2) //50 entries per ticker
      }
    }

    describe("multiple indicators") {
      val testHelper = new StockTradeAnalyserSpecHelper(Sma50.periodSpan)
      val indicatorsToCompute = Vector(Sma10, Sma50)
      testHelper.stockTradeAnalyser.analyse (
        config = Config(marketDataDir = "./foo"),
        tickers = NonEmptyVector(ticker3dp, Vector(ticker3pl)),
        indicators = indicatorsToCompute,
        dataFrameSize = None
      )

      it("should analyse only specified tickers") {
        testHelper.dataBuffer.map(_.stockMarketEntry.ticker.value).distinct should contain only (ticker3dp.value, ticker3pl.value)
      }

      it("should compute expected number of moving averages") {
        val indicatorEntries = testHelper.dataBuffer

        indicatorEntries.length should ===(testHelper.dataBuffer.length)
        indicatorEntries.length should ===(50 * 2) //50 entries per ticker
      }

      it("should contain all indicators") {
        val indicatorEntries = testHelper.dataBuffer.map(_.indicators.size)

        indicatorEntries.forall(_ == indicatorsToCompute.size) should ===(true)
      }
    }
  }

}

private class StockTradeAnalyserSpecHelper(periodSpanToGenerate: Int) {

  val dataBuffer = new ListBuffer[EnrichedStockMarketEntry]()

  private def extractDummyRawData(path: Path): Task[Vector[RawStockMarketData]] = {
    Task.now {
      Iterator.range(1, periodSpanToGenerate * 2).toVector.map(value =>
        RawStockMarketData(f"$value%02d", Random.nextString(20))
      )
    }
  }

  private def parseToTestData(rawData: RawStockMarketData): Vector[ErrorOr[StockMarketEntry]] = {
    val price = Price(rawData.fileName.toDouble)
    Vector(
      StockMarketEntry(Ticker("3DP"), Datestamp("20180531"), price, price),
      StockMarketEntry(Ticker("3PL"), Datestamp("20180531"), price, price),
      StockMarketEntry(Ticker("XYZ"), Datestamp("20180531"), price, price)
    ).map(Right(_))
  }

  private def enrichData(iType: IndicatorType, batch: Seq[Vector[StockMarketEntry]]): Vector[EnrichedStockMarketEntry] = {
    batch.flatMap(entries => {
      val maybeAvg = if (entries.length == iType.periodSpan) Some(entries.map(_.close.value).sum / iType.periodSpan) else None
      entries.lastOption.map(
        EnrichedStockMarketEntry(_, maybeAvg.map(avg => Indicator(iType, IndicatorValue(avg))).toSet)
      )
    }).toVector
  }

  private def reportData: (Vector[EnrichedStockMarketEntry], Option[Int]) => Task[Unit] = (enrichedEntries, _) => {
   Task {
     dataBuffer.append(enrichedEntries: _*)
   }
  }

  val stockTradeAnalyser = new StockTradeAnalyser(
    extractRawData = extractDummyRawData,
    parseRawData = parseToTestData,
    enrichData = enrichData,
    reportData = reportData
  )

}
