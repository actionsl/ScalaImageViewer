package org.actionsl.utils

import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import org.junit._

class ClosuresTest  extends JUnitSuite {

  private val THREAD_PAUSE = 500
  
   private val FILE_ROOT = "testing"
   private val SUFFIX = "jpg"
 
  @Test def cTimeTest(){
    
    val tempTimer = Closures.cTimer
    
    Thread.sleep(THREAD_PAUSE)
    assertTrue(tempTimer()>=THREAD_PAUSE)
  }
  
  @Test def fileNameMakerTest(){
    
    val nameMaker = Closures.fileNameMaker(FILE_ROOT, SUFFIX)
    
    assertEquals(nameMaker(),FILE_ROOT + "0" + "." + SUFFIX )
    assertEquals(nameMaker(),FILE_ROOT + "1" + "." + SUFFIX )
    assertEquals(nameMaker(),FILE_ROOT + "2" + "." + SUFFIX )
  }
}