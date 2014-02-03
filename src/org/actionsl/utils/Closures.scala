package org.actionsl.utils

object Closures {

   // returns the time since initialisation
  def cTimer = { // A timing closure
    val startTime = System.currentTimeMillis
    () => System.currentTimeMillis - startTime
  }

  // Generate incremental filenames using a counter e.g.name001.jpg, name002. jpg ..
  // Use: val nameMaker = fileNameMaker("name000","jpg") ; nameMaker()
  def fileNameMaker = (rootName: String, extn: String) => {
    val length = rootName.length
    var counter = -1 // Need to start at 0
    () => {
      counter += 1
      val len = counter.toString.length
      rootName.slice(0, length - len+1) + counter.toString + "." + extn
    }
  }
}