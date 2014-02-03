package org.actionsl.images

import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import org.junit._

class FileImageCacheTest extends JUnitSuite {

  private val TEST_NAME = "tester"
  private val nullFileImage = FileImage(TEST_NAME, null)

  @Test def addTest() {
    val fic = new FileImageCache
    assertEquals(fic.size, 0)
    fic.add(List(nullFileImage))
    assertEquals(fic.size, 1)
    fic.add(List(nullFileImage))
    assertEquals(fic.size, 1)

  }

  @Test def getTest() {
    val fic = new FileImageCache
    fic.add(List(nullFileImage))
    assertEquals(fic.get(List(TEST_NAME)).length, 1)
    assertEquals(fic.get(List(TEST_NAME)).head.name, TEST_NAME)

  }
}