package moe.brianhsu.live2d.demo.swt.widget

import moe.brianhsu.live2d.demo.app.{DemoApp, LanguageManager}
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
    loadAvatar, changeToDefaultBackground,
    selectBackground, pureColorBackground, transparentBackground, languageSelector) = createToolItems()
    transparentBackground.setSelection(DemoApp.loadTransparentBackground())

  {
    this.setLayout(new FillLayout)
    // 初始化语言管理器
    LanguageManager.initializeFromDemoApp()
    
    loadAvatar.addListener(SWT.Selection, { (event: Event) => openLoadAvatarDialog(event) })
    selectBackground.addListener(SWT.Selection, { (event: Event) => onSelectBackgroundSelected(event) })
    changeToDefaultBackground.addListener(SWT.Selection, { (event: Event) => onDefaultBackgroundSelected(event) })
    pureColorBackground.addListener(SWT.Selection, { (event: Event) => onPureColorBackground(event) })
    transparentBackground.addListener(SWT.Selection, { (event: Event) => onTransparentBackground(event) })
    languageSelector.addListener(SWT.Selection, { (event: Event) => onLanguageSelected(event) })
  }

  def setDemoApp(demoApp: DemoApp): Unit = {
    this.demoAppHolder = Some(demoApp)
    val enabled = DemoApp.loadTransparentBackground()
    demoApp.setTransparentBackground(enabled)
  }

  private def onSelectBackgroundSelected(event: Event): Unit = {
    val fileDialog = new FileDialog(parent.getShell, SWT.OPEN)
    fileDialog.setFilterExtensions(Array("*.png;*.PNG", "*.jpg;*.jpeg;*.JPG;*.JPEG"))

    for {
      demoApp <- demoAppHolder
      selectedFile <- Option(fileDialog.open())
    } {
      demoApp.changeBackground(selectedFile).failed.foreach { _ =>
        val messageBox = new MessageBox(parent.getShell, SWT.OK|SWT.ICON_ERROR)
        messageBox.setText(LanguageManager.getText("message.cannot_load_background"))
        messageBox.setMessage(LanguageManager.getText("message.unsupported_file_type"))
        messageBox.open()
      }
    }
  }

  private def onPureColorBackground(event: Event): Unit = {
    val colorChooser = new ColorDialog(parent.getShell)
    colorChooser.setText(LanguageManager.getText("message.select_background_color"))
    colorChooser.setRGB(new RGB(0, 255, 0))

    for {
      demoApp <- demoAppHolder
      rgb <- Option(colorChooser.open())
      color = new Color(rgb.red, rgb.green, rgb.blue)
    } {
      demoApp.switchToPureColorBackground(color)
    }
  }

  private def onTransparentBackground(event: Event): Unit = {
    val enabled = transparentBackground.getSelection
    demoAppHolder.foreach(_.setTransparentBackground(enabled))
    DemoApp.saveTransparentBackground(enabled)
  }

  private def onDefaultBackgroundSelected(event: Event): Unit = {
    demoAppHolder.foreach(_.switchToDefaultBackground())
    transparentBackground.setSelection(false)
    DemoApp.saveTransparentBackground(false)
  }

  private def onLanguageSelected(event: Event): Unit = {
    val menu = new Menu(parent.getShell, SWT.POP_UP)
    
    LanguageManager.getSupportedLanguages.foreach { case (language, displayName) =>
      val menuItem = new MenuItem(menu, SWT.RADIO)
      menuItem.setText(displayName)
      menuItem.setSelection(LanguageManager.getCurrentLanguage == language)
      menuItem.addListener(SWT.Selection, { (_: Event) =>
        LanguageManager.setLanguage(language)
        DemoApp.saveLanguage(language.toString.toLowerCase)
        updateUITexts()
      })
    }
    
    val location = languageSelector.getBounds
    val point = toolBar.toDisplay(location.x, location.y + location.height)
    menu.setLocation(point)
    menu.setVisible(true)
  }

  private def openLoadAvatarDialog(event: Event): Unit = {
    val directoryDialog = new DirectoryDialog(parent.getShell, SWT.OPEN)
    val selectedDirectoryHolder = Option(directoryDialog.open())
    for {
      demoApp <- demoAppHolder
      selectedDirectory <- selectedDirectoryHolder
    } {
      demoApp.switchAvatar(selectedDirectory).failed.foreach { e =>
        val messageBox = new MessageBox(parent.getShell, SWT.OK|SWT.ICON_ERROR)
        messageBox.setText(LanguageManager.getText("message.cannot_load_avatar"))
        messageBox.setMessage(e.getMessage)
        messageBox.open()
      }
    }
  }


  private def createToolItems(): (ToolItem, ToolItem, ToolItem, ToolItem, ToolItem, ToolItem) = {
    val loadAvatar = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val defaultBackground = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val selectBackground = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val pureColorBackground = new ToolItem(toolBar, SWT.PUSH)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val transparentBackground = new ToolItem(toolBar, SWT.CHECK)
    new ToolItem(toolBar, SWT.SEPARATOR)
    val languageSelector = new ToolItem(toolBar, SWT.DROP_DOWN)
    transparentBackground.setSelection(DemoApp.loadTransparentBackground())

    loadAvatar.setText(LanguageManager.getText("toolbar.load_avatar"))
    defaultBackground.setText(LanguageManager.getText("toolbar.green_background"))
    selectBackground.setText(LanguageManager.getText("toolbar.select_background"))
    pureColorBackground.setText(LanguageManager.getText("toolbar.pure_color_background"))
    transparentBackground.setText(LanguageManager.getText("toolbar.transparent_background"))
    languageSelector.setText(LanguageManager.getText("toolbar.language"))

    (loadAvatar, defaultBackground, selectBackground, pureColorBackground, transparentBackground, languageSelector)
  }

  private def updateUITexts(): Unit = {
    loadAvatar.setText(LanguageManager.getText("toolbar.load_avatar"))
    changeToDefaultBackground.setText(LanguageManager.getText("toolbar.green_background"))
    selectBackground.setText(LanguageManager.getText("toolbar.select_background"))
    pureColorBackground.setText(LanguageManager.getText("toolbar.pure_color_background"))
    transparentBackground.setText(LanguageManager.getText("toolbar.transparent_background"))
    languageSelector.setText(LanguageManager.getText("toolbar.language"))
  }
}
