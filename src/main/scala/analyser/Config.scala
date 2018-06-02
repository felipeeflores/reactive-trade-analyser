package analyser

case class Config(marketDataDir: String)

object Config {

  def apply(): Config = {
    Config(sys.env.getOrElse("MARKET_DATA_DIR", "./data/"))
  }
}
