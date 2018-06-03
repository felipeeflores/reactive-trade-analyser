package analyser.model

sealed trait IndicatorType {
  val periodSpan: Int
}

case object Sma10 extends IndicatorType {
  override val periodSpan: Int = 10

  override def toString: String = "10SMA"
}

case object Sma50 extends IndicatorType {
  override val periodSpan: Int = 50

  override def toString: String = "50SMA"
}
