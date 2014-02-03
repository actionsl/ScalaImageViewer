package org.actionsl.main

import javax.swing.SwingUtilities

object Main {

  def createAndShowUIDemo() {
    AppMainImages.top.visible = true
  }

  def main(args: Array[String]): Unit = {
    SwingUtilities.invokeLater(new Runnable {	
      def run = createAndShowUIDemo()
    })
  }
}
