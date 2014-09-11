package strixy

object Implicits {
  implicit class Pipe[T](value: T) {
    def |>[R](f: T => R) = f(value)
  }
}
