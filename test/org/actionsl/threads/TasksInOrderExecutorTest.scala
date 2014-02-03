package org.actionsl.threads

import org.scalatest.junit.JUnitSuite

import org.junit.Assert._
import org.junit._

class TasksInOrderExecutorTest extends JUnitSuite {

  def fn1(): Int = 10
  def fn2(): Int = {
    Thread.sleep(2000)    
    val x = 10 + 10
    x
  }
  def fn3(): Int = {
    30
  }
  def fn4(): Int = {
    Thread.sleep(3000)       
    40
  }
  
  @Test def getTest() {

    val list = List[() => Int](fn1, fn2, fn3, fn4)
    
    val tioe = TasksInOrderExecutor(list)
    
    val results = tioe.results
    assertEquals(4, results.length)
    assertEquals(10, results(0))
    assertEquals(20, results(1))
    assertEquals(30, results(2))
    assertEquals(40, results(3))
    
  }
}