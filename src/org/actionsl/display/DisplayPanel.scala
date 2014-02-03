package org.actionsl.display

import scala.swing._
import scala.swing.event._

import java.awt.{ Color, RenderingHints }
import java.awt.event.MouseEvent

import org.actionsl.images._
import org.actionsl.utils.{ History, Logging, GuiTools }

/**
 * Class holds the list of images to be displayed and controls viewing by handling the mouse clicks
 *  next image - right hand mouse button
 *  previous image - left hand mouse button
 *
 *  @param	images 	holds images to be displayed
 *  @param	size 	default size for the initial display :java.awt.Dimension
 */
class DisplayPanel(private var images: List[FileImage] = List[FileImage](), size: Dimension) extends Panel with Publisher with Logging {

  private val BLUE_PANEL_TEXT = " Use File menu (Alt F) to choose pics to display"

  minimumSize = size
  preferredSize = size
  focusable = true

  private val history = History[List[String]]
  private val cache = new FileImageCache(images)

  private var index = 0 // current index of image being viewed
  private var clipboard: FileImage = _ // used for cut and paste

  var imgSize = size

  history.push(getImageNames(images))

  def historyClear = history.clear // Convenience

  def getImages = images // private images

  def getImageNames(list: List[FileImage]) = list map { _.name } // functional

  def setFileImages(filename: String, suffixes: List[String]) {
    { // Cognitive brackets - collects related code visually
      val fileImages = ImageHelper.loadImages(ImageHelper.selectFiles(filename, suffixes))
      if (fileImages.isEmpty) return
      cache.add(fileImages)
      images = fileImages
      index = 0
    }
    history.push(getImageNames(images))
    repaint
  }
  
  def setImages(fileImages: List[FileImage]) {
    {
      if (fileImages.isEmpty) return
      cache.add(fileImages)
      images = fileImages
      index = 0
    }
    history.push(getImageNames(images))
    repaint
  }
  
  def addFileImages(filename: String, suffixes: List[String]) = {
    {
      val fileImages = ImageHelper.loadImages(ImageHelper.selectFiles(filename, suffixes))

      if (!fileImages.isEmpty) {
        cache.add(fileImages)
        if (images.isDefinedAt(index + 1)) {
          val (left, right) = images.splitAt(index + 1)
          index += 1
          images = left ++ fileImages ++ right
        } else {
          index = if (images.length > 0) images.length else 0
          images = images ++ fileImages
        }
      }
    }
    history.push(getImageNames(images))
    repaint
  }

  def undo() {
    history.undo match {
      case Some(keys) =>
        images = cache.get(keys)
        repaint
      case None =>
    }
  }

  def redo() {
    history.redo match {
      case Some(keys) =>
        images = cache.get(keys)
        repaint
      case None =>
    }
  }

  def cutImage() {
    {
      if (images.isEmpty) return // Guard against cutting from empty list 
      val (left, right) = images.splitAt(index)
      clipboard = right.head
      images = left ++ right.tail
    }
    history.push(getImageNames(images))
    repaint
  }

  def copyImage() {
    if (images.isEmpty) return // Guard against pasting from empty list 
    clipboard = images(index)
  }

  def pasteImage() {
    {
      if (null == clipboard) return // Guard against pasting from null clipboard 
      val (left, right) = images.splitAt(index)
      images = left ++ List(clipboard) ++ right
    }
    history.push(getImageNames(images))
    repaint
  }

  def deleteImage() {
    {
      if (images.isEmpty) return // Guard against deleting from empty list 
      val (left, right) = images.splitAt(index)
      images = left ++ right.tail
    }
    history.push(getImageNames(images))
    repaint
  }

  def nextImagePaint() {
    nextImage
    repaint
  }

  private def prevImagePaint() {
    prevImage
    repaint
  }

  private def nextImage = if (index < images.length - 1) index += 1 else index = 0
  private def prevImage = if (index > 0) index -= 1 else index = images.length - 1

  override def paintComponent(g2: Graphics2D) {

    val width = imgSize.getWidth().asInstanceOf[Int]
    val height = imgSize.getHeight().asInstanceOf[Int]

    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON)

    if (images.isEmpty) {
      val buffer = GuiTools.makeImage(Color.BLUE, Color.RED, size, BLUE_PANEL_TEXT)
      g2.drawImage(buffer, 0, 0, buffer.getWidth, buffer.getHeight, null)
    } else {
      if (index >= images.length) index = images.length - 1 // Guard for cut at end of list
      g2.drawImage(images(index).image, 0, 0, width, height, null)
    }
  }
  // Image selected using mouse: leftButton(previous), rightButton(next)
  listenTo(mouse.clicks, mouse.moves)
  reactions += {
    case e: MouseReleased =>
      e.peer.getButton() match {
        case MouseEvent.BUTTON3 => nextImagePaint
        case MouseEvent.BUTTON1 => prevImagePaint
      }
  }

  listenTo(keys)
  reactions += {
    case KeyReleased(_, Key.Right, _, _) => nextImagePaint
    case KeyPressed(_, Key.Left, _, _) => prevImagePaint
  }
}

object DisplayPanel {

  def apply(images: List[FileImage], size: Dimension) = new DisplayPanel(images, size)

}
