package org.actionsl.threads

import java.util.concurrent.{ Callable, Executors, Future, TimeUnit }
import scala.collection.mutable.ListBuffer

/**
 * Executes a List of ()=>T functions on numThreads threads with all results being returned within timeout ms
 */
class TasksInOrderExecutor[T] private[threads] (taskFns: List[() => T], numThreads: Int, timeout: Long) {

  import TasksInOrderExecutor._

  private val exec = Executors.newFixedThreadPool(numThreads)	// start the threads and get the executor
  
  private val futures = ListBuffer[Future[T]]()	// container to hold the results

  taskFns foreach { task => futures += exec.submit(task) } // add tasks - use the implicit conversion function2Callable

  shutdown // executes all submitted tasks before shutting down

  private def shutdown = exec.shutdown()	// shutdown when all tasks completed

  /*
   * returns results for the tasks - only public method in the class
   */
  def results = futures map { _.get(timeout, TimeUnit.MILLISECONDS) }	// return the future results

}

object TasksInOrderExecutor {
  
  private val DEFAULT_NUM_THREADS = 20 // compromise/pragmatic
  private val DEFAULT_TIME_OUT = 5000L // milliseconds
   
  implicit def function2Callable[T](x: () => T): Callable[T] = new Callable[T] { def call(): T = x() }
 
  def apply[T](taskFns: List[() => T], numThreads: Int = DEFAULT_NUM_THREADS, timeout: Long = DEFAULT_TIME_OUT ) = new TasksInOrderExecutor(taskFns, numThreads, timeout)
}