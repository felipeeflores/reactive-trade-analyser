package analyser

import analyser.ConsoleOps.printOK
import analyser.model.{EnrichedStockMarketEntry, IndicatorType}
import monix.eval.Task

object Reporter {

  def reportResult(results: Vector[EnrichedStockMarketEntry], limit: Option[Int], indicatorTypes: Vector[IndicatorType]): Task[Unit] = {
    Task {
      printOK("SYM,DATE,EOD," + indicatorTypes.map(_.toString).mkString(","))
      results.slice(0, limit.getOrElse(Int.MaxValue)).foreach(result =>
        printOK(
          f"${result.stockMarketEntry.ticker.value},${result.stockMarketEntry.date.value},${result.stockMarketEntry.close.value}%.3f," +
          indicatorTypes.map(indicatorType =>
            f"${result.indicators.find(_.indicatorType == indicatorType).map(_.indicatorValue.value).getOrElse(0.0)}%.3f"
          ).mkString(",")
        )
      )
    }
  }
}
