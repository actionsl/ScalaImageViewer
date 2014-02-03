package org.actionsl.utils

import java.awt.image.BufferedImage
import java.io.{ File, IOException }
import javax.imageio.ImageIO
import java.util.Properties
import java.io.FileInputStream

object Tools extends Logging {

  //fileType: ("GIF", "JPEG", ("PNG")  
  def writeImgToFile(img: BufferedImage, fileType: String, filename: String) {

    log.info("output file %s fileType %s".format(filename, fileType))

    val set = Set[String]("GIF", "JPEG", "JPG", "PNG")
    require(set.contains(fileType.toUpperCase()))
    try {
      val file = new File(filename)
      if (!ImageIO.write(img, fileType, file)) {
        throw new Exception("Error writing img file " + filename)
      }
    } catch {
      case ex: IOException => log.error(ex.getMessage())
      case ex: Exception => log.error(ex.getMessage())
    }
  }
  /*
 * Load properties file and return a scala Map
 */
  def readConfigFile(filename: String): Map[String, String] = {
    val prop = new Properties()
    try {
      prop.load(new FileInputStream(filename))
    } catch {
      case ex: IOException => log.warn(ex.getMessage())
      case ex: Exception => log.warn(ex.getMessage())
    }
    scala.collection.JavaConversions.asScalaMap(prop).toMap
  }

  /*
   * Simple try catch wrapper for function f
   */
  def tryCatchLog(f: => Unit) {
    try {
      f
    } catch {
      case ex: Exception => log.warn(ex.getMessage())
    }
  }

  // Put the exception stacktrace in a string
  def stringStackTrace(ex: Exception): String = { // local fn here to make tryCatch 'self contained'     
    val sw = new java.io.StringWriter()
    ex.printStackTrace(new java.io.PrintWriter(sw))
    sw.toString();
  }

  /*
   * Control wrapper that gives access to PrintWriter and performs cleanup  
   */
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}
  
  //  thumbs
  //  Image img = ImageIO.read(new File("test.jpg")).getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH)
  //  
  //  BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
  //img.createGraphics().drawImage(ImageIO.read(new File("test.jpg")).getScaledInstance(100, 100, Image.SCALE_SMOOTH),0,0,null);
  //ImageIO.write(img, "jpg", new File("test_thumb.jpg"));