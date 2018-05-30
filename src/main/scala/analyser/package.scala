package object analyser {

  type ErrorOr[A] = Either[AppError, A]
}
