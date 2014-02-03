package org.actionsl.images

import java.awt.image.BufferedImage

/*
 * FileImage - holds the filename BufferedImage pair that is used for
 * representing images in the application
 */
case class FileImage(name: String, image: BufferedImage){
  def this(pair: Pair[String,BufferedImage]) = this(pair._1,pair._2)	// Legacy - may still be useful
}