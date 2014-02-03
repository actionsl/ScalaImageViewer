package org.actionsl.main

import swing._
import swing.event.UIElementResized

import org.actionsl.display._
import org.actionsl.images.ImageHelper._
import org.actionsl.threads.TimedExecutorWrapper

import org.actionsl.utils.Tools

object ConfigViewerDefaults {

  val CONFIG_FILE = "resources/viewer.properties"

  // Common default values
  val DEFAULT_SCREEN_SIZE = new Dimension(800, 800)
  val DEFAULT_IMAGE_FILE_SUFFIXES = List[String]("jpg", "jpeg")

  // Defaults for viewer.properties
  val DEFAULT_DIRECTORY = "." //
  val REFRESH_INTERVAL = "1000" // Note : String - 1000ms
  val IMAGE_LIST_FILE = "" // no file name
}

object AppMainImages extends SimpleSwingApplication {

  import ConfigViewerDefaults._

  // Configuration  
  private val propsMap = Tools.readConfigFile(CONFIG_FILE)

  private val initialDirectory = propsMap.getOrElse("viewer.default.directory", DEFAULT_DIRECTORY)
  private val refreshIntervalMs = propsMap.getOrElse("viewer.refresh.interval", REFRESH_INTERVAL).toInt
  private val imageListFile = propsMap.getOrElse("viewer.image.file", IMAGE_LIST_FILE)

  // Initialise BufferedImage list with filenames from imageListFile OR return List[BufferedImage].empty
  private val listFileImages = loadImageFiles(imageListFile, DEFAULT_IMAGE_FILE_SUFFIXES)

  private val displayPanel = DisplayPanel(listFileImages, DEFAULT_SCREEN_SIZE)

  // stand-alone thread to loop through the images
  private val viewer = TimedExecutorWrapper(displayPanel.nextImagePaint, refreshIntervalMs)

  // New Frame
  def top = new MainFrame {

    title = "Display Images Swing App"

    contents = new BoxPanel(Orientation.Vertical) { // Create the enclosing display panel
      border = Swing.EmptyBorder(0, 0, 0, 0)
      menuBar = new MainMenuBar(initialDirectory)
      contents += rootPanel(displayPanel)
    }

    listenTo(displayPanel)
    reactions += {
      case UIElementResized(top) => displayPanel.imgSize_=(top.peer.getSize())
      case _ =>
    }

    // can be reset in the menu
    private var fileSuffixes = DEFAULT_IMAGE_FILE_SUFFIXES 

    listenTo(menuBar)
    reactions += {
      
      case muEvent: MenuFileEvent => displayPanel.setFileImages(muEvent.filename, fileSuffixes)
      case muEvent: MenuAddFileEvent => displayPanel.addFileImages(muEvent.filename, fileSuffixes)
      case muEvent: MenuDirEvent => displayPanel.setFileImages(muEvent.dirname, fileSuffixes)
      case muEvent: MenuAddDirEvent => displayPanel.addFileImages(muEvent.dirname, fileSuffixes)
      case muEvent: MenuFileSuffixesEvent => {
        fileSuffixes_=(muEvent.suffixes)
        log.info("Using new file suffixes " + fileSuffixes)
      }
     
      case muEvent: MenuEditUndoEvent => displayPanel.undo
      case muEvent: MenuEditRedoEvent => displayPanel.redo
      case muEvent: MenuEditClearHistoryEvent =>displayPanel.historyClear
       
      case muEvent: MenuEditCutEvent => displayPanel.cutImage
      case muEvent: MenuEditCopyEvent => displayPanel.copyImage
      case muEvent: MenuEditPasteEvent => displayPanel.pasteImage
      case muEvent: MenuEditDeleteEvent => displayPanel.deleteImage
      
      case muEvent: MenuViewerStartEvent => viewer.start
      case muEvent: MenuViewerStopEvent => viewer.stop
      case muEvent: MenuViewerRefreshEvent => viewer.setRefresh(muEvent.interval)      
      case muEvent: MenuViewerSaveShowEvent =>       
         Tools.printToFile(muEvent.file) { p => displayPanel.getImages foreach {  fileImage =>  p.println( fileImage.name) } }
      case muEvent: MenuViewerOpenShowEvent => displayPanel.setImages(loadImageFiles(muEvent.filename, fileSuffixes))
      
      case _ =>
    }
  }

  private def rootPanel(display: Component): Panel = {

    new GridBagPanel() { // TODO: Legacy layout - seems to work OK
      val gbc = new Constraints()

      gbc.anchor = GridBagPanel.Anchor.Center;
      gbc.grid = (0, 0)
      gbc.fill = GridBagPanel.Fill.Both;
      gbc.gridheight = 3;
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      add(display, gbc);
    } //panel    
  }
}
