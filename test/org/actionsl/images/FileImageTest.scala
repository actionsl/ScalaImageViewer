package org.actionsl.images

import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import org.junit._


class FileImageTest extends JUnitSuite {

  private val TEST_NAME = "tester"
    
  @Test def FileImageCreate {
    
    val fm = FileImage(TEST_NAME,null)
    assertEquals(fm.name, TEST_NAME)
    assertNull(fm.image)
 }
}