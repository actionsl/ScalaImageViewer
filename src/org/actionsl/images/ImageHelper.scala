package org.actionsl.images

import java.io.File
import java.io.IOException

import scala.collection.mutable.ListBuffer

import org.actionsl.threads.TasksInOrderExecutor
import org.actionsl.utils.Closures
import org.actionsl.utils.Logging

import javax.imageio.ImageIO

/**
 * Object holding common code for handling images
 */
object ImageHelper extends Logging {

  ImageIO.setUseCache(false) // The FileImageCache stores any downloaded files

  /**
   * Loads images from a list of filename give n in a specifies file
   *
   * @param	filename	File containing the list of images to be loaded
   * @param	suffixes	Used to filter files based upon their suffix e.g. jpg
   * @return	List of FileImage(s) found in filename abd filtered using suffixews
   */
  def loadImageFiles(filename: String, suffixes: List[String]): List[FileImage] = {
    val file = new File(filename)
    if (file.exists()) {
      val results = scala.io.Source.fromFile(filename).getLines() map // read the file list
        { s => ImageHelper.selectFiles(s, suffixes) } map //select files based on their suffix
        { list => ImageHelper.loadImages(list) } //toList 	// load files get a list of FileImages
      log.info(filename + " loaded")
      results.toList.flatten
    } else {
      log.warn(filename + " could not be loaded")
      List.empty[FileImage]
    }
  }
  /**
   * Loads images from the list of Files given and returns a list of BufferedImages
   *
   * @param	files	List of File objects to be used as BufferedImages
   * @return	List of BufferedImages obtained by reading the File objects
   */
  def zloadImages(files: List[File]): List[FileImage] = {

    val fileImages = new ListBuffer[FileImage]()

    val loadImageTimer = Closures.cTimer

    files foreach { file =>
      try {
        fileImages += FileImage(file.getAbsolutePath(), ImageIO.read(file))
      } catch {
        case ex: IOException => log.warn(ex + " filename: " + file.getAbsolutePath())
        case ex: Exception => log.warn(ex)
      }
    }
    //log.info("loaded %d files in %d ms".format(files.length, loadImageTimer.apply))
    fileImages.toList
  }
  /**
   * Loads images from the list of Files given and returns a list of BufferedImages
   *
   * @param	files	List of File objects to be used as BufferedImages
   * @return	List of BufferedImages obtained by reading the File objects
   */
  //  def parallelLoadImages(files: List[File]): List[FileImage] = {
  def loadImages(files: List[File]): List[FileImage] = {

    val loadingTimer = Closures.cTimer

    // Create the parallel tasks
    val list = files map { file => () => FileImage(file.getAbsolutePath(), ImageIO.read(file)) }

    // execute the parallel tasks
    val tioe = TasksInOrderExecutor(list)
    val results = tioe.results

    //log.info("parallel loaded %d files in %d ms".format(files.length, loadingTimer()))
    results.toList
  }
  /**
   * Selects filename strings to see if they have a recognised suffix. If the name is a file
   * the individual file is checked. If its a directory all files that satisfy the suffixes
   * in the directory are read
   *
   * @param	name	filename or directory name
   * @param	suffixes	valid file suffixes e.g List("jpg", "jpeg")
   * @return	the selected List of File(s)
   */
  def selectFiles(name: String, suffixes: List[String]): List[File] = {

    val hasValidSuffix = (file: File) => // predicate determining if file name has valid suffix
      suffixes.exists { suffix => file.getName().toLowerCase.endsWith(suffix) }

    val file = new File(name)
    var results = List.empty[File] // Used as return value - removes a few lines of code

    if (file.isDirectory()) {
      results = file.listFiles() filter { hasValidSuffix } toList
    } else if (file.isFile()) {
      if (hasValidSuffix(file)) results = List[File](file)
      else log.info(name + " is not recognised as a valid file name. Soln: Set the suffixes")
    }
    results
  }
}
