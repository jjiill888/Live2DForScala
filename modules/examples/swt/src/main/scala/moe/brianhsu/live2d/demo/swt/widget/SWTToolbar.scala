package moe.brianhsu.live2d.demo.swt.widget

import moe.brianhsu.live2d.demo.app.DemoApp
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets._

import java.awt.Color
import scala.annotation.unused

class SWTToolbar(parent: Composite) extends Composite(parent, SWT.NONE) {
  private var demoAppHolder: Option[DemoApp] = None
  private val toolBar = new ToolBar(this, SWT.NONE)
  private val (
    loadAvatar, defaultAvatar, changeToDefaultBackground,
    selectBackground, pureColorBackground, transparentBackground) = createToolItems()
    transparentBackground.setSelection(DemoApp.loadTransparentBackground())

  {
    this.setLayout(new FillLayout)
    loadAvatar.addListener(SWT.Selection, openLoadAvatarDialog)
    selectBackground.addListener(SWT.Selection, onSelectBackgroundSelected)
    changeToDefaultBackground.addListener(SWT.Selection, onDefaultBackgroundSelected)
    pureColorBackground.addListener(SWT.Selection, onPureColorBackground)
    transparentBackground.addListener(SWT.Selection, onTransparentBackground)
    defaultAvatar.addListener(SWT.Selection, onDefaultAvatarSelected)
  }

  def setDemoApp(demoApp: DemoApp): Unit = {
    this.demoAppHolder = Some(demoApp)
    val enabled = DemoApp.loadTransparentBackground()
    demoApp.setTransparentBackground(enabled)
  }

  private def onSelectBackgroundSelected(@unused event: Event): Unit = {
    val fileDialog = new FileDialog(parent.getShell, SWT.OPEN)
    fileDialog.setFilterExtensions(Array("*.png;*.PNG", "*.jpg;*.jpeg;*.JPG;*.JPEG"))

    for {
      demoApp <- demoAppHolder
      selectedFile <- Option(fileDialog.open())
    } {
      demoApp.changeBackground(selectedFile).failed.foreach { _ =>
        val messageBox = new MessageBox(parent.getShell, SWT.OK|SWT.ICON_ERROR)
        messageBox.setText("Cannot load background")
        messageBox.setMessage("Unsupported file type.")
        messageBox.open()
      }
    }
  }

  private def onPureColorBackground(@unused event: Event): Unit = {
    val colorChooser = new ColorDialog(parent.getShell)
    colorChooser.setText("Select Background Color")
    colorChooser.setRGB(new RGB(0, 255, 0))

    for {
      demoApp <- demoAppHolder
      rgb <- Option(colorChooser.open())
      color = new Color(rgb.red, rgb.green, rgb.blue)
    } {
      demoApp.switchToPureColorBackground(color)
    }
  }

  private def onTransparentBackground(@unused event: Event): Unit = {
    val enabled = transparentBackground.getSelection
    demoAppHolder.foreach(_.setTransparentBackground(enabled))
    DemoApp.saveTransparentBackground(enabled)
  }

  private def onDefaultBackgroundSelected(@unused event: Event): Unit = {
    demoAppHolder.foreach(_.switchToDefaultBackground())
    transparentBackground.setSelection(false)
    DemoApp.saveTransparentBackground(false)
  }

  private def openLoadAvatarDialog(@unused event: Event): Unit = {
    val directoryDialog = new DirectoryDialog(parent.getShell, SWT.OPEN)
    val selectedDirectoryHolder = Option(directoryDialog.open())
    for {
      demoApp <- demoAppHolder
      selectedDirectory <- selectedDirectoryHolder
    } {
      demoApp.switchAvatar(selectedDirectory).failed.foreach { e =>
        val messageBox = new MessageBox(parent.getShell, SWT.OK|SWT.ICON_ERROR)
        messageBox.setText("Cannot load avatar.")
        messageBox.setMessage(e.getMessage)
        messageBox.open()
      }
    }
  }

  private def onDefaultAvatarSelected(@unused event: Event): Unit = {
    val defaultAvatarPath = "def_avatar" // jar包同目录
    demoAppHolder.foreach { demoApp =>
      demoApp.switchAvatar(defaultAvatarPath).failed.foreach { e =>
        val messageBox = new MessageBox(parent.getShell, SWT.OK | SWT.ICON_ERROR)
        messageBox.setText("Cannot load default avatar.")
        messageBox.setMessage(e.getMessage)
        messageBox.open()
      }
    }
  }

  private def createToolItems(): (ToolItem, ToolItem, ToolItem, ToolItem, ToolItem, ToolItem) = {
    val loadAvatar = new ToolItem(toolBar, SWT.PUSH)
    val defaultAvatar = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val defaultBackground = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val selectBackground = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val pureColorBackground = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val transparentBackground = new ToolItem(toolBar, SWT.CHECK)
    transparentBackground.setSelection(DemoApp.loadTransparentBackground())

    loadAvatar.setText("Load Avatar")
    defaultAvatar.setText("Default Avatar")
    defaultBackground.setText("Green Background")
    selectBackground.setText("Select Background")
    pureColorBackground.setText("Pure Color Background")
    transparentBackground.setText("Transparent Background")

    (loadAvatar, defaultAvatar, defaultBackground, selectBackground, pureColorBackground, transparentBackground)
  }
}
