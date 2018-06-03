package analyser

import analyser.model.{IndicatorType, Sma10, Sma50, Ticker}

import scala.util.Try

case class CommandLineArgs(symbols: Vector[Ticker], days: Option[Int] = None, indicators: Vector[IndicatorType])

object CommandLineArgs {

  def apply(args: Array[String]): Option[CommandLineArgs] = {

    def parseTickers(raw: String): Vector[Ticker] = raw.split(",").map(Ticker).toVector

    def parseDays(raw: String): Option[Int] = Try(raw.toInt).fold(_ => None, Some(_))

    def parseIndicators(raw: String): Vector[IndicatorType] = {
      val splitSides = raw.split("INDICATORS=").toList
      val maybeRhs = splitSides.tail.headOption

      maybeRhs match {
        case Some(rhs) => rhs.split(",").toVector.flatMap { rawIndicator =>
          rawIndicator.toUpperCase match {
            case "10SMA" => Some(Sma10)
            case "50SMA" => Some(Sma50)
            case _ => None
          }
        }
        case None => Vector.empty
      }
    }

    args.toList match {
      case head :: Nil => Some(CommandLineArgs(parseTickers(head), None, Vector.empty))
      case head :: second :: Nil => Some(CommandLineArgs(parseTickers(head), parseDays(second), parseIndicators(second)))
      case head :: second :: third :: Nil => Some(CommandLineArgs(parseTickers(head), parseDays(second), parseIndicators(third)))
      case _ => None
    }
  }

}