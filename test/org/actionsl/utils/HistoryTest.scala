package org.actionsl.utils

import org.scalatest.junit.JUnitSuite

import org.junit.Assert._
import org.junit._

class HistoryTest extends JUnitSuite {

  private val N = 10
  private var history = new History[Int]()

  @Test def push {

    Range(0, N) foreach history.push _

    assertEquals(history.undoTop, N - 1)
    assertEquals(history.undo, Some(N - 2))
    assertEquals(history.redo, Some(N - 1))
    assertEquals(history.undoTop, N - 1)

    history.clear

    assertEquals(history.undo, None)
    assertEquals(history.redo, None)

  }
}