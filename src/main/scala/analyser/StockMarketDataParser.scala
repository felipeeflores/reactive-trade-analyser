package analyser

import analyser.model.{RawStockMarketData, StockMarketEntry}
import kantan.csv._
import kantan.csv.ops._

object StockMarketDataParser {

  def parse(rawData: RawStockMarketData): Vector[ErrorOr[StockMarketEntry]] = {
    val parseResult = rawData.fileData.asCsvReader[StockMarketEntry](rfc.withoutHeader).toVector

    parseResult.map {
      case Right(value) => Right(value)
      case Left(error) => Left(InvalidCsvData(s"Invalid csv given: ${rawData.fileName} => ${error.getMessage}"))
    }
  }
}
