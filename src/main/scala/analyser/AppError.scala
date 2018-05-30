package analyser

sealed trait AppError

case class InvalidCsvData(message: String) extends AppError
