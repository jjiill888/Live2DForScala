package moe.brianhsu.live2d.demo.swing.widget

import moe.brianhsu.live2d.demo.swing.Live2DUI
import moe.brianhsu.live2d.demo.app.DemoApp

import java.awt.Color
import java.awt.event.ActionEvent
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing._
import scala.annotation.unused

class SwingToolbar(live2DWidget: Live2DUI) extends JToolBar("Live 2D For Scala Swing Toolbar") {

  private val loadAvatar = new JButton("Load Avatar")
  private val pureBackground = new JButton("Pure Color Background")
  private val defaultBackground = new JButton("Green Background")
  private val selectBackground = new JButton("Select Background Image")
  private val transparentBackground = new JToggleButton("Transparent Background")
  transparentBackground.setSelected(DemoApp.loadTransparentBackground())

  {
    this.loadAvatar.addActionListener { (event: ActionEvent) => loadAvatarAction(event) }
    this.defaultBackground.addActionListener { (event: ActionEvent) => switchToDefaultBackground(event) }
    this.pureBackground.addActionListener { (event: ActionEvent) => switchToPureColor(event) }
    this.selectBackground.addActionListener { (event: ActionEvent) => changeBackground(event) }
    this.transparentBackground.addActionListener { (event: ActionEvent) => switchToTransparent(event) }
    this.add(loadAvatar)
    this.add(defaultBackground)
    this.add(selectBackground)
    this.add(pureBackground)
    this.add(transparentBackground)
  }

  private def changeBackground(actionEvent: ActionEvent): Unit = {

    val jpgFilter = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg")
    val pngFilter = new FileNameExtensionFilter("PNG file", "png")
    val fileChooser = new JFileChooser()
    fileChooser.setFileFilter(pngFilter)
    fileChooser.addChoosableFileFilter(jpgFilter)
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)

    val result = fileChooser.showOpenDialog(this.getParent)
    if (result == JFileChooser.APPROVE_OPTION) {
      val filePath = fileChooser.getSelectedFile.getAbsolutePath
      if (filePath.toLowerCase.endsWith("png") ||
          filePath.toLowerCase.endsWith("jpg") ||
          filePath.toLowerCase.endsWith("jpeg")) {

        for {
          live2D <- live2DWidget.demoAppHolder
          exception <- live2D.changeBackground(filePath).failed
        } {
          exception.printStackTrace()
          showErrorMessage("Cannot load background", "File format not supported.")
        }
      } else {
        showErrorMessage("Cannot load background", "File format not supported.")
      }
    }


  }

  private def switchToDefaultBackground(actionEvent: ActionEvent): Unit = {
    live2DWidget.demoAppHolder.foreach(_.switchToDefaultBackground())
    transparentBackground.setSelected(false)
    DemoApp.saveTransparentBackground(false)
  }

  private def switchToPureColor(actionEvent: ActionEvent): Unit = {
    for {
      live2d <- live2DWidget.demoAppHolder
      selectedColor <- Option(JColorChooser.showDialog(this.getParent, "Choose Background", new Color(0.0f, 1.0f, 0.0f)))
    } {
      live2d.switchToPureColorBackground(selectedColor)
    }
  }

  private def switchToTransparent(actionEvent: ActionEvent): Unit = {
    val enabled = transparentBackground.isSelected
    live2DWidget.demoAppHolder.foreach(_.setTransparentBackground(enabled))
    DemoApp.saveTransparentBackground(enabled)
  }
  
  private def loadAvatarAction(action: ActionEvent): Unit = {
    val fileChooser = new JFileChooser()
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
    val result = fileChooser.showOpenDialog(this.getParent)
    if (result == JFileChooser.APPROVE_OPTION) {
      live2DWidget.demoAppHolder.foreach { view =>
        val avatarHolder = view.switchAvatar(fileChooser.getSelectedFile.getAbsolutePath)
        avatarHolder.failed.foreach(e => showErrorMessage("Cannot load avatar", e.getMessage))
      }
    }
  }

  private def showErrorMessage(title: String, message: String): Unit = {
    JOptionPane.showMessageDialog(this.getParent, message, title, JOptionPane.ERROR_MESSAGE)
  }

}
