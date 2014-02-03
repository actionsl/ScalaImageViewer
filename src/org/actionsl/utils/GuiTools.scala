package org.actionsl.utils

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Font}

object GuiTools {
  /**
   * Creates a BufferedImage with specified background and text colors a
   * java.awt.Dimension size and text that is displayed in the centre/upper half of image
   */
  def makeImage(backgroundColor: Color, textColor: Color, size: Dimension, text: String): BufferedImage = {

    // TODO this should be in a utility somewhere
    val buffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB)
    val g2 = buffer.createGraphics();

    g2.setColor(backgroundColor)
    g2.fillRect(0, 0, buffer.getWidth(), buffer.getHeight())
    g2.setPaint(textColor)
    g2.setFont(new Font("Serif", Font.BOLD, 28))

    val fm = g2.getFontMetrics()
    val posX = buffer.getWidth() / 2 - fm.stringWidth(text) / 2
    val posY = buffer.getHeight() / 6

    g2.drawString(text, posX, posY);
    g2.dispose()
    buffer
  }
}