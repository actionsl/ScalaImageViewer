package org.actionsl.utils

import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import org.junit._

import java.io.File

class ToolsTest {
  
  private val TEST_PROPS_FILE_NAME  = """resources/test.properties"""    
    
  @Before def checks() {
   if(!new File(TEST_PROPS_FILE_NAME).exists) println("Can't find the file")
  }
  
  @Test def readConfigFileTest(){
    val map = Tools.readConfigFile(TEST_PROPS_FILE_NAME)
    assert("1000" == map.getOrElse("viewer.refresh.interval", "unknown"))
    assert("unknown" == map.getOrElse("what", "unknown"))
  }
}