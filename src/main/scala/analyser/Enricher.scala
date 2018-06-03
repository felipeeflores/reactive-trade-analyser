package analyser

import analyser.model.{EnrichedStockMarketEntry, Indicator, IndicatorType, StockMarketEntry}

object Enricher {

  def enrichStockDataWithIndicator(indicatorType: IndicatorType, batch: Seq[Vector[StockMarketEntry]]): Vector[EnrichedStockMarketEntry] = {
    val indicatorEnrichedData: Seq[Option[EnrichedStockMarketEntry]] = batch.map(marketEntries => {
      val indicators: Option[Indicator] = Indicators.computeSimpleMovingAverage(indicatorType, marketEntries)
      marketEntries.lastOption.map(entry =>
        EnrichedStockMarketEntry(entry, indicators.toSet)
      )
    })
    indicatorEnrichedData.collect {
      case Some(indicator) => indicator
    }.toVector
  }
}
