package analyser.model

//fields currently not in use are ignored
case class StockMarketEntry(ticker: Ticker, date: Datestamp, open: Price, close: Price)

object StockMarketEntry {

  import kantan.csv._

  implicit val stockMarketEntryDecoder: RowDecoder[StockMarketEntry] =
    RowDecoder.ordered {
      (tickerStr: String, dateStr: String, openPrice: Double, _: Double, _: Double, closePrice: Double, _: Long) => {
        StockMarketEntry(
          Ticker(tickerStr),
          Datestamp(dateStr),
          Price(openPrice),
          Price(closePrice)
        )
      }
    }
}
