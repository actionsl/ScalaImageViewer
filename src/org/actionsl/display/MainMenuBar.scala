package org.actionsl.main

import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.io.File

import scala.swing.Action
import scala.swing.CheckBox
import scala.swing.FileChooser
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Publisher
import scala.swing.event.Event

import org.actionsl.utils.Logging

import javax.swing.JOptionPane
import javax.swing.KeyStroke.getKeyStroke

sealed abstract class MenuEvent extends Event

case class MenuFileEvent(filename: String) extends MenuEvent
case class MenuDirEvent(dirname: String) extends MenuEvent
case class MenuAddFileEvent(filename: String) extends MenuEvent
case class MenuAddDirEvent(dirname: String) extends MenuEvent
case class MenuFileSuffixesEvent(suffixes: List[String]) extends MenuEvent

case class MenuViewerStartEvent extends MenuEvent
case class MenuViewerStopEvent extends MenuEvent
case class MenuViewerRefreshEvent(interval: Int) extends MenuEvent
case class MenuViewerSaveShowEvent(file: File) extends MenuEvent
case class MenuViewerOpenShowEvent(filename: String) extends MenuEvent

case class MenuEditUndoEvent() extends MenuEvent
case class MenuEditRedoEvent() extends MenuEvent
case class MenuEditClearHistoryEvent() extends MenuEvent

case class MenuEditCutEvent() extends MenuEvent
case class MenuEditCopyEvent() extends MenuEvent
case class MenuEditPasteEvent() extends MenuEvent
case class MenuEditDeleteEvent() extends MenuEvent

class MainMenuBar(private var defaultFile: String) extends MenuBar with Publisher with Logging {

  private val DEFAULT_SUFFIXES = List("jpg", "jpeg", "gif", "png")

  private val MIN_INTERVAL_MS = 10

  private val NO_SHORTCUT = 0

  private val shortcutKeyMask = Toolkit.getDefaultToolkit.getMenuShortcutKeyMask

  // Helper utility
  private def makeMenuItem(title: String, fn: => Unit, shortCut: Int = NO_SHORTCUT, toolTipText: String = ""): MenuItem = {
    new MenuItem(new Action(title) {
      if (shortCut != NO_SHORTCUT) accelerator = Some(getKeyStroke(shortCut, shortcutKeyMask))
      if (toolTipText.length > 0) this.toolTip = toolTipText
      def apply = fn
    })
  }

  private val checkBoxes = makeMenuCheckBoxes(DEFAULT_SUFFIXES: _*) // used for file suffixes

  private def makeMenuCheckBoxes(texts: String*): Seq[CheckBox] =
    texts map { text => new CheckBox(text) { peer.setSelected(true) } } // pre-select all to true

  contents += new Menu("File") {

    this.peer.setMnemonic(KeyEvent.VK_F) // Alt F to pull up the menu

    //    contents += makeMenuItem("Open File", chooseFile("Choose File to Display"))
    contents += makeMenuItem("Open Directory", chooseDirectory("Choose Directory to Display"), KeyEvent.VK_O, toolTipText = "Choose directory images - overwrites current images")
    contents += makeMenuItem("Add File", chooseFile("Choose File to Add to Display", true), KeyEvent.VK_A, "Adds image after currently viewed image")
    contents += makeMenuItem("Add Directory", chooseDirectory("Choose Directory to Add to Display", true), KeyEvent.VK_D, "Adds directory images after currently viewed image")

    peer.addSeparator()
    contents += makeMenuItem("Open Picture Series File . . ", chooseShowFile("Choose File"))

    contents += new Menu("File Suffixes") { // menu inside a menu

      // this idiom keeps the same 'contents' ordering of arguments
      (contents /: checkBoxes)(_ += _) 

      peer.addSeparator()
      contents += makeMenuItem("OK?", fileSuffixes) // confirm ok - publish event
    }
    peer.addSeparator()
    contents += new MenuItem(new Action("Exit") { def apply = System.exit(0) }) // Old fashioned way without makeMenuItem
  }

  private def fileSuffixes() { // callback for the checkbox 'OK?'
    val seq = checkBoxes filter { _.peer.isSelected() } map { _.text }
    publish(MenuFileSuffixesEvent(seq.toList))
  }

  contents += new Menu("Edit") {

    this.peer.setMnemonic(KeyEvent.VK_E) // Alt E to pull up the menu

    contents += makeMenuItem("Undo", undo, KeyEvent.VK_Y)
    contents += makeMenuItem("Redo", redo, KeyEvent.VK_Z)

    peer.addSeparator()
    contents += makeMenuItem("Clear History", clear, toolTipText = "Clear undo/redo history ")

    peer.addSeparator()
    contents += makeMenuItem("Cut", cut, KeyEvent.VK_X)
    contents += makeMenuItem("Copy", copy, KeyEvent.VK_C, "Make copy of displayed image")
    contents += makeMenuItem("Paste", paste, KeyEvent.VK_V, "Insert before displayed image")

    peer.addSeparator()
    contents += makeMenuItem("Delete", delete, toolTipText = "Delete displayed image")
  }

  private def undo = publish(MenuEditUndoEvent()) // Don't explicitly call threads inside actions
  private def redo = publish(MenuEditRedoEvent())
  private def clear = publish(MenuEditClearHistoryEvent())

  private def cut = publish(MenuEditCutEvent())
  private def copy = publish(MenuEditCopyEvent())
  private def paste = publish(MenuEditPasteEvent())
  private def delete = publish(MenuEditDeleteEvent())

  contents += new Menu("Viewer") {

    this.peer.setMnemonic(KeyEvent.VK_W) // Alt E to pull up the menu

    contents += makeMenuItem("Start Viewer", start)
    contents += makeMenuItem("Stop Viewer", stop)

    peer.addSeparator()
    contents += makeMenuItem("Refresh Interval (ms) . . .", inputInteger)
    peer.addSeparator()
    //contents += makeMenuItem("Open Image Show file", chooseShowFile("Choose Viewer Show File"))
    contents += makeMenuItem("Save picture series to file . . .", chooseSaveFile("Save picture sequence"))
  }

  private def start = publish(MenuViewerStartEvent()) // You don't explicitly call threads inside actions
  private def stop = publish(MenuViewerStopEvent())

  private def saveFile = new FileChooser().showSaveDialog(this)

  private def inputInteger = { // TODO: Clumsy
    try {
      val result = JOptionPane.showInputDialog("Refresh interval (ms)").toInt
      if (result > MIN_INTERVAL_MS) publish(MenuViewerRefreshEvent(result)) // emit event
      else log.warn(result + "ms interval to short. Min Interval = " + MIN_INTERVAL_MS + "ms")
    } catch {
      case ex: NumberFormatException => log.warn(ex.getMessage())
      case ex: Exception => log.warn(ex.getMessage())
    }
  }

  /**
   * chooseFile - uses FileChooser to allow a file to be selected either for adding or
   * overwriting the current BufferedImage list. Emits the appropriate event
   * @param	title	- displayed border title for FileChooser
   * @param	add	- append the chosen directory contents or overwrite
   */

  private def chooseFile(title: String = "", add: Boolean = false) {

    val fc = new FileChooser(new File(defaultFile))
    fc.title = title

    if (fc.showOpenDialog(null) == FileChooser.Result.Approve) {
      defaultFile = fc.selectedFile.getParent() //getAbsolutePath()
      if (add) publish(MenuAddFileEvent(fc.selectedFile.getAbsolutePath()))
      else publish(MenuFileEvent(fc.selectedFile.getAbsolutePath()))
    }
  }

  /**
   * Saves a list of file names
   */
  private def chooseSaveFile(title: String = "") {

    val fc = new FileChooser(new File("."))
    fc.title = title

    if (fc.showSaveDialog(null) == FileChooser.Result.Approve) {
      publish(MenuViewerSaveShowEvent(new File(fc.selectedFile.getAbsolutePath())))
    }
  }

  /**
   * Open file containing a list of image file
   */
  private def chooseShowFile(title: String = "") {

    val fc = new FileChooser(new File("."))
    fc.title = title

    if (fc.showOpenDialog(null) == FileChooser.Result.Approve) {
      publish(MenuViewerOpenShowEvent(fc.selectedFile.getAbsolutePath()))
    }
  }

  /**
   * chooseDirectory - uses FileChooser to allow a directory to be selected either for adding or
   * overwriting the current BufferedImage list. Emits the appropriate event
   * @param	title	- displayed border title for FileChooser
   * @param	add	- append the chosen directory contents or overwrite
   */
  private def chooseDirectory(title: String = "", add: Boolean = false) {

    val fc = new FileChooser(new File(defaultFile))
    fc.title = title
    fc.fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly

    if (fc.showOpenDialog(null) == FileChooser.Result.Approve) {
      defaultFile = fc.selectedFile.getParent() //getAbsolutePath()
      if (add) publish(MenuAddDirEvent(fc.selectedFile.getCanonicalPath()))
      else publish(MenuDirEvent(fc.selectedFile.getCanonicalPath()))
    }
  }
}
