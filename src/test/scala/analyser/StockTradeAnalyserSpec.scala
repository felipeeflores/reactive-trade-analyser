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
    val testHelper = new StockTradeAnalyserSpecHelper()

    val ticker3dp = Ticker("3DP")
    val ticker3pl = Ticker("3PL")
    testHelper.stockTradeAnalyser.analyse(
      config = Config(marketDataDir = "./foo"),
      tickers = NonEmptyVector(ticker3dp, Vector(ticker3pl)),
      indicators = Vector.empty,
      dataFrameSize = None
    )

    it("should analyse only specified tickers") {
      testHelper.dataBuffer.map(_.stockMarketEntry.ticker.value).distinct should contain only (ticker3dp.value, ticker3pl.value)
    }

    it("should compute expected number of moving averages") {
      val sma10Entries = testHelper.dataBuffer.filter(entry => entry.indicators.exists(i => i.indicatorType == Sma10))
      sma10Entries.length should ===(10 * 2) //ten entries per ticker
    }

    it("should compute 10SMA indicators") {
      val sma10Entries = testHelper.dataBuffer.filter(entry => entry.indicators.exists(_.indicatorType == Sma10))

      val indicators = sma10Entries.flatMap(_.indicators).toVector
      val expectedIndicatorValues = Iterator.range(5, 15).map(n => Indicator(Sma10, IndicatorValue(n + 0.5))).toVector

      indicators should contain only (expectedIndicatorValues: _*)
    }

    it("should compute 50SMA indicators") {
      //TODO
    }
  }

}

private class StockTradeAnalyserSpecHelper {

  val dataBuffer = new ListBuffer[EnrichedStockMarketEntry]()

  private def extractDummyRawData(path: Path): Task[Vector[RawStockMarketData]] = {
    Task.now {
      Iterator.range(1, Sma10.periodSpan * 2).toVector.map(value =>
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

  private def enrichData(batch: Seq[Vector[StockMarketEntry]]): Vector[EnrichedStockMarketEntry] = {
    batch.flatMap(entries => {
      val maybeAvg = if (entries.length == Sma10.periodSpan) Some(entries.map(_.close.value).sum / Sma10.periodSpan) else None
      entries.lastOption.map(
        EnrichedStockMarketEntry(_, maybeAvg.map(avg => Indicator(Sma10, IndicatorValue(avg))).toSet)
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
