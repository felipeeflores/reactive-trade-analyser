package analyser

import analyser.model.RawStockMarketData
import monix.eval.Task

import scala.io.Source
import scala.reflect.io.Path

object RawStockMarketDataExtractor {

  def extractRawData(path: Path): Task[Vector[RawStockMarketData]] = {
    Task {
      val sortedFiles = retrieveFiles(path)
      sortedFiles.map {
        case (fileName, path) => RawStockMarketData(fileName, Source.fromFile(path.jfile).mkString)
      }
    }
  }

  private def retrieveFiles(path: Path): Vector[(String, Path)] = {
    val stockPriceFilePattern = """\d{8}\.txt$"""
    path.walk
      .filter(filePath => filePath.name.matches(stockPriceFilePattern))
      .map(path => (path.name, path)).toVector
      .sortBy({ case (name, _) => name })
  }
}
