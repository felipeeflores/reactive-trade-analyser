package analyser

case class Config(pricesPath: String)

object Config {

  def apply(): Config = {
    Config(sys.env.getOrElse("PRICES_PATH", "./data/"))
  }
}
