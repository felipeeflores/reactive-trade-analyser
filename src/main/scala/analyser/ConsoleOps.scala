package analyser

import scala.Console.{GREEN, RED, RESET}

object ConsoleOps {

  private val usage =
    """
      |Usage:
      |
      |> export MARKET_DATA_DIR=<path-to-stock-data>
      |>
      |> ./analyser $SYMBOLS [[optional] $DAYS] [[optional] IND=$INDICATORS]
      |
      |where:
      |\t $SYMBOLS
      |\t\t List of ASX ticker codes separated by comma (i.e. AEE, AGO, PLS)
      |\t $DAYS
      |\t\t Size of the data frame in days; it is provided as an integer
      |\t $INDICATORS
      |\t\t List of supported indicators that user want to see included in the final report (i.e. 10SMA, 50SMA, 10EMA)
    """.stripMargin

  def printError(error: String) = Console.println(s"${RESET}${RED}$error${RESET}")

  def printOK(message: String) = Console.println(s"${RESET}${GREEN}$message${RESET}")

  def printInfo(message: String) = Console.println(s"$message")

  def printInvalidCommandMessageUsage(): Unit = printInfo(
    s"""
      |Invalid command provided.
      |
      |$usage
    """.stripMargin)

}
