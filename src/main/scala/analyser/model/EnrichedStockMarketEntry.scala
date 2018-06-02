package analyser.model

case class EnrichedStockMarketEntry(stockMarketEntry: StockMarketEntry, indicators: Set[Indicator])
