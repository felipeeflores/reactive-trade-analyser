package analyser

import analyser.model.IndicatorType

sealed trait AppError

case class InvalidCsvData(message: String) extends AppError
case class InvalidDataSetForIndicator(requestedIndicator: IndicatorType, dataSetLength: Int) extends AppError
