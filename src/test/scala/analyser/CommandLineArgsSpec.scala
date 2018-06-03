package analyser

import analyser.model.{Sma10, Sma50, Ticker}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FunSpec, Matchers, OptionValues}

class CommandLineArgsSpec extends FunSpec with Matchers with TypeCheckedTripleEquals with OptionValues {

  describe("CommandLineArgs") {

    describe("smart constructor") {

      it("should parse to None if empty args") {
        val args = Array.empty[String]
        val result = CommandLineArgs(args)

        result should ===(None)
      }

      it("should parse a symbol") {
        val args = Array("ABC")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), None, Vector.empty))
      }

      it("should parse multiple symbols") {
        val args = Array("ABC,XYZ,FOOBAR")
        val result = CommandLineArgs(args)

        result.value should === (
          CommandLineArgs(
            symbols = Vector(
              Ticker("ABC"),
              Ticker("XYZ"),
              Ticker("FOOBAR")
            ),
            days = None,
            indicators = Vector.empty
          )
        )
      }

      it("should parse DAYS") {
        val args = Array("ABC","100")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), Some(100), Vector.empty))
      }

      it("should ignore invalid DAYS") {
        val args = Array("ABC","TEN")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), None, Vector.empty))
      }

      it("should parse an INDICATOR with no DAYS argument"){
        val args = Array("ABC","INDICATORS=10SMA")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), None, Vector(Sma10)))
      }

      it("should parse an INDICATOR"){
        val args = Array("ABC","100","INDICATORS=50SMA")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), Some(100), Vector(Sma50)))
      }

      it("should parse multiple INDICATORS"){
        val args = Array("ABC","100","INDICATORS=10SMA,50SMA")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), Some(100), Vector(Sma10, Sma50)))
      }

      it("should ignore invalid INDICATORS"){
        val args = Array("ABC","100","10SMA,50SMA")
        val result = CommandLineArgs(args)

        result.value should === (CommandLineArgs(Vector(Ticker("ABC")), Some(100), Vector.empty))
      }

    }

  }
}
