package analyser

import analyser.StockTradeAnalyser.{handleErrors, printInfo, printPrime}
import analyser.model._
import cats.data.NonEmptyVector
import cats.implicits._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{Consumer, Observable}

import scala.Console.{GREEN, RED, RESET}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.io.Path


class StockTradeAnalyser(extractRawData: Path => Task[Vector[RawStockMarketData]],
                         parseRawData: RawStockMarketData => Vector[ErrorOr[StockMarketEntry]],
                         enrichData: Seq[Vector[StockMarketEntry]] => Vector[EnrichedStockMarketEntry],
                         reportData: (Vector[EnrichedStockMarketEntry], Option[Int]) => Task[Unit]
                        ) {

  def analyse(config: Config,
              tickers: NonEmptyVector[Ticker],
              indicators: Vector[IndicatorType],
              dataFrameSize: Option[Int]) = {

    val rawDataObservable: Observable[Vector[RawStockMarketData]] = Observable.fromTask(extractRawData(Path(config.marketDataDir)))

    val stockMarketData: Observable[Vector[StockMarketEntry]] = rawDataObservable.mapTask(rawData =>
      Task {
        val (errors, stockMarketData) = rawData.flatMap(parseRawData).separate
        handleErrors(errors)
        stockMarketData
      }
    )

    val filteredMarketData: Observable[Map[Ticker, Vector[StockMarketEntry]]] = stockMarketData.mapTask { entries =>
      Task {
        entries.filter(entry => tickers.toVector.contains(entry.ticker)).groupBy(_.ticker)
      }
    }

    val sma10EnrichedData = filteredMarketData.map { tickerData =>
      tickerData.flatMap {
        case (_, data) => enrichData(data.sliding(Sma10.periodSpan, 1).toSeq)
      }.toVector
    }

    val reporter = sma10EnrichedData.mapTask(data => reportData(data, dataFrameSize))

    val task = reporter
      .consumeWith(Consumer.complete)
      .materialize

    Await.result(task.runAsync, Duration.Inf)

    printInfo("Execution finished")

  }

}

object StockTradeAnalyser {

  private def printError(error: String) = Console.println(s"${RESET}${RED}$error${RESET}")

  private def printPrime(message: String) = Console.println(s"${RESET}${GREEN}$message${RESET}")

  private def printInfo(message: String) = Console.println(s"$message")

  /**
    * Errors are a first class citizen there should be something done when invalid entry is found in the source data.
    * Nothing has being specified as a requirement, so just printing out to system.err
    *
    * @param errors
    * @return
    */
  def handleErrors(errors: Vector[AppError]) = Task.eval {
    errors.foreach({
      case InvalidCsvData(message) => printError(s"Invalid CSV in source data: $message")
      case InvalidDataSetForIndicator(requestedIndicator, dataSetLength) =>
        printError(s"Invalid/incomplete data set of size $dataSetLength for $requestedIndicator")
    })
  }

  def apply(): StockTradeAnalyser = {

    val enrichData: Seq[Vector[StockMarketEntry]] => Vector[EnrichedStockMarketEntry] = batch => {
      printInfo("Processing batch of size " + batch.size)
      val sma10EnrichedData: Seq[Option[EnrichedStockMarketEntry]] = batch.map(marketEntries => {
        printInfo(s"market entries ${marketEntries.length}")
        val indicators: Option[Indicator] = Indicators.computeSimpleMovingAverage(Sma10, marketEntries)
        marketEntries.lastOption.map(entry =>
          EnrichedStockMarketEntry(entry, indicators.toSet)
        )
      })
      val result = sma10EnrichedData.collect {
        case Some(indicator) => indicator
      }
      printInfo(s"resulted in $result")
      result.toVector
    }



    val reporter = (results: Vector[EnrichedStockMarketEntry], _: Option[Int]) => Task {
      printPrime("SYM,DATE,EOD,10SMA")
      results.foreach(result => printPrime(
        s"${result.stockMarketEntry.ticker},${result.stockMarketEntry.date},${result.stockMarketEntry.close},${result.indicators.find(_.indicatorType == Sma10)}"
      ))
    }

    new StockTradeAnalyser(
      RawStockMarketDataExtractor.extractRawData,
      RawStockMarketDataParser.parse,
      enrichData,
      reporter
    )
  }
}
