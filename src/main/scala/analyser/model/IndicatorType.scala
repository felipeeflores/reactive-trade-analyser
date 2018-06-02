package analyser.model

sealed trait IndicatorType {
  val periodSpan: Int
}

case object Sma10 extends IndicatorType {
  override val periodSpan: Int = 10
}

case object Sma50 extends IndicatorType {
  override val periodSpan: Int = 50
}
