package org.actionsl.threads

/*
 * TimedExecutorWrapper - provides a control wrapper for the SimpleTimedExecutor
 * The SimpleTimedExecutor should always be used through this wrapper
 * 
 * The loop can be started and stopped using start/stop and the interval can be changed with setRefresh 
 * 
 * For a different function start a new TimedExecutorWrapper
 */

class TimedExecutorWrapper(fn: => Unit, interval: Long) {

  private var executor: SimpleTimedExecutor = null
  private var refreshInterval = interval

  def start = if (null == executor) { // check executor not running
    executor = SimpleTimedExecutor(fn, refreshInterval)
    executor.start
  }

  def stop = if (null != executor) { // check executor is running
    executor.stop
    executor = null
  }

 /*
 * setRefresh - set a new refresh interval in milliseconds
 */
  def setRefresh(interval: Int) {
    stop
    refreshInterval = interval
  }
}

object TimedExecutorWrapper {
  def apply(fn: => Unit, intervalMillis: Long) = new TimedExecutorWrapper(fn, intervalMillis)
}
