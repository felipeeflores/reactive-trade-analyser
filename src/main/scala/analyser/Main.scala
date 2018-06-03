package analyser

import cats.data.NonEmptyVector

object Main {

  def main(args: Array[String]): Unit = {

    val maybeCommandLineArgs = CommandLineArgs(args)

    maybeCommandLineArgs match {
      case Some(commandLineArgs) =>
        val maybeTickers = NonEmptyVector.fromVector(commandLineArgs.symbols)
        val analyser = StockTradeAnalyser()
        maybeTickers.foreach(tickers =>
          analyser.analyse(Config(), tickers, commandLineArgs.indicators, commandLineArgs.days)
        )
      case None => ConsoleOps.printInvalidCommandMessageUsage()
    }


  }
}
