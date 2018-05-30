package analyser

import monix.execution.Scheduler.Implicits.global
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.io.Path

class RawStockMarketDataExtractorSpec extends FunSpec with Matchers with TypeCheckedTripleEquals {

  describe("RawStockMarketDataExtractor") {
    val testResourcePath = getClass.getResource("/").getPath
    val task = RawStockMarketDataExtractor.extractRawData(Path(testResourcePath))

    val result = Await.result(task.runAsync, 5.seconds)

    describe("for the given path") {
      it("should extract all data") {
        result.size should ===(4)
      }
      it ("should extract files in order") {
        result.map(_.fileName) should contain inOrder ("20180102.txt", "20180103.txt", "20180108.txt", "20180109.txt")
      }
    }
  }

}
