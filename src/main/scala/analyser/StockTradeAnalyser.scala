package analyser

import analyser.StockTradeAnalyser.handleErrors
import analyser.model._
import cats.data.NonEmptyVector
import cats.implicits._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{Consumer, Observable}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.io.Path


class StockTradeAnalyser(extractRawData: Path => Task[Vector[RawStockMarketData]],
                         parseRawData: RawStockMarketData => Vector[ErrorOr[StockMarketEntry]],
                         enrichData: (IndicatorType, Seq[Vector[StockMarketEntry]]) => Vector[EnrichedStockMarketEntry],
                         reportData: (Vector[EnrichedStockMarketEntry], Option[Int], Vector[IndicatorType]) => Task[Unit]
                        ) {

  def analyse(config: Config,
              tickers: NonEmptyVector[Ticker],
              indicators: Vector[IndicatorType],
              dataFrameSize: Option[Int]) = {

    val indicatorsToCompute = if (indicators.nonEmpty) indicators else Vector(Sma10, Sma50)

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

    val combinedData: Observable[Vector[EnrichedStockMarketEntry]] = filteredMarketData.map { tickerData =>
      indicatorsToCompute.flatMap { indicatorType =>
        tickerData.flatMap {
          case (_, data) => enrichData(indicatorType, data.sliding(indicatorType.periodSpan, 1).toSeq)
        }.toVector
      }
    }

    val aggregatedData = combinedData.map { enrichedData =>
      enrichedData.groupBy(_.stockMarketEntry).map {
        case (entry, enrichedEntry) => (entry, enrichedEntry.flatMap(_.indicators))
      }.collect {
        case (entry, computedIndicators) if computedIndicators.length == indicatorsToCompute.length =>
          EnrichedStockMarketEntry(entry, computedIndicators.toSet)
      }.toVector
    }

    val reporter = aggregatedData.mapTask(data => reportData(data, dataFrameSize, indicatorsToCompute))

    val task = reporter
      .consumeWith(Consumer.complete)
      .materialize

    Await.result(task.runAsync, Duration.Inf)

  }

}

object StockTradeAnalyser {

  import ConsoleOps._

  /**
    * Errors are a first class citizen there should be something done when invalid entry is found in the source data.
    * Nothing has being specified as a requirement, so just printing out to system.out
    *
    * @param errors
    * @return
    */
  def handleErrors(errors: Vector[AppError]) = Task.eval {
    errors.foreach({
      case InvalidCsvData(message) => printError(s"Invalid CSV in source data: $message")
    })
  }

  def apply(): StockTradeAnalyser = {

    new StockTradeAnalyser(
      RawStockMarketDataExtractor.extractRawData,
      RawStockMarketDataParser.parse,
      Enricher.enrichStockDataWithIndicator,
      Reporter.reportResult
    )
  }
}
