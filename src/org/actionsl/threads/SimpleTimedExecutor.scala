package org.actionsl.threads

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * SimpleTimedExecutor - executes the function fn every intervalMillis ms. 
 * Can only be used inside threads package and is provided for the user in a wrapper
 */
class SimpleTimedExecutor private[threads](fn: => Unit, intervalMillis: Long) {

  import SimpleTimedExecutor._
  
  private val exec = Executors.newSingleThreadScheduledExecutor() // Create the executable thread  

  private[threads] def start = exec.scheduleWithFixedDelay(fn , 0, intervalMillis, TimeUnit.MILLISECONDS) // run the function - use implicit function2Runnable 

  private[threads] def stop = exec.shutdown() 
}

object SimpleTimedExecutor {
  
  implicit def function2Runnable[T](x: => T): Runnable = new Runnable { def run = x } 

  private[threads] def apply(fn: => Unit, intervalMillis: Long ) = new SimpleTimedExecutor(fn, intervalMillis)
  
}

