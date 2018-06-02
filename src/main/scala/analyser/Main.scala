package analyser

import analyser.model.Ticker
import cats.data.NonEmptyVector

object Main {

  def main(args: Array[String]): Unit = {
    val analyser = StockTradeAnalyser()

    analyser.analyse(Config(), NonEmptyVector(Ticker("1AD"), Vector.empty), Vector.empty, None)
  }
}
