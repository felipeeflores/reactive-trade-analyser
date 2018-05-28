package example

import monix.eval.Task
import monix.reactive.Observable
import monix.reactive.Consumer
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object Hello extends Greeting with App {
  println(greeting)

  val observable = Observable.fromIterable(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
  
  val task1 = observable.
    bufferSliding(5, 1).
    mapTask(numbers => Task {
      val res = numbers.sum / 5
      println(s"numbers: $numbers, avg: $res")
    }).
    consumeWith(Consumer.complete).
    materialize

  val task2 = observable.
    bufferSliding(10, 1).
    mapTask(numbers => Task {
      val res = numbers.sum / 5
      println(s"numbers: $numbers, avg: $res")
    }).
    consumeWith(Consumer.complete).
    materialize

  Await.result(Task.gather(List(task1, task2)).runAsync, Duration.Inf)
}

trait Greeting {
  lazy val greeting: String = "hello"
}
