package moe.brianhsu.live2d.demo.javafx.widget

import javafx.scene.control.{Alert, Button, ButtonType, CheckBox, ColorPicker, Dialog, ToolBar}
import javafx.stage.{DirectoryChooser, FileChooser, Window}
import javafx.scene.paint.{Color => JFXColor}
import moe.brianhsu.live2d.demo.app.DemoApp

import java.awt.Color
import scala.annotation.unused

class JavaFXToolbar extends ToolBar {
  this.setId("main-toolbar")

  private var demoAppHolder: Option[DemoApp] = None

  private val loadAvatar       = new Button("Load Avatar")
  private val defaultBackground = new Button("Green Background")
  private val selectBackground  = new Button("Select Background")
  private val pureColorBackground = new Button("Pure Color Background")
  private val transparentBackground = new CheckBox("Transparent Background")
  transparentBackground.setSelected(DemoApp.loadTransparentBackground())

  this.getItems.addAll(
    loadAvatar,
    defaultBackground,
    selectBackground,
    pureColorBackground,
    transparentBackground
  )

  loadAvatar.setOnAction(_ => openLoadAvatarDialog())
  defaultBackground.setOnAction(_ => onDefaultBackgroundSelected())
  selectBackground.setOnAction(_ => onSelectBackgroundSelected())
  pureColorBackground.setOnAction(_ => onPureColorBackground())
  transparentBackground.setOnAction(_ => onTransparentBackground())

  def setDemoApp(demoApp: DemoApp): Unit = {
    this.demoAppHolder = Some(demoApp)
    val enabled = DemoApp.loadTransparentBackground()
    demoApp.setTransparentBackground(enabled)
  }

  private def openLoadAvatarDialog(): Unit = {
    val chooser = new DirectoryChooser()
    val window = currentWindow
    val selected = Option(chooser.showDialog(window))
    for {
      demoApp <- demoAppHolder
      dir <- selected
    } {
      demoApp.switchAvatar(dir.getAbsolutePath).failed.foreach { e =>
        val alert = new Alert(Alert.AlertType.ERROR)
        alert.setTitle("Cannot load avatar.")
        alert.setContentText(e.getMessage)
        alert.showAndWait()
      }
    }
  }


  private def onSelectBackgroundSelected(): Unit = {
    val chooser = new FileChooser()
    chooser.getExtensionFilters.addAll(
      new FileChooser.ExtensionFilter("PNG Image", "*.png", "*.PNG"),
      new FileChooser.ExtensionFilter("JPEG Image", "*.jpg", "*.jpeg", "*.JPG", "*.JPEG")
    )
    val window = currentWindow
    val selected = Option(chooser.showOpenDialog(window))
    for {
      demoApp <- demoAppHolder
      file <- selected
    } {
      demoApp.changeBackground(file.getAbsolutePath).failed.foreach { _ =>
        val alert = new Alert(Alert.AlertType.ERROR)
        alert.setTitle("Cannot load background")
        alert.setContentText("Unsupported file type.")
        alert.showAndWait()
      }
    }
  }

  private def onPureColorBackground(): Unit = {
    val picker = new ColorPicker(JFXColor.GREEN)
    val dialog = new Dialog[JFXColor]()
    dialog.setTitle("Select Background Color")
    dialog.getDialogPane.setContent(picker)
    dialog.getDialogPane.getButtonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
    dialog.setResultConverter((bt: ButtonType) => if (bt == ButtonType.OK) picker.getValue else null)
    val result = dialog.showAndWait()
    if (result.isPresent) {
      val fxColor = result.get()
      val awtColor = new Color((fxColor.getRed * 255).toInt, (fxColor.getGreen * 255).toInt, (fxColor.getBlue * 255).toInt)
      demoAppHolder.foreach(_.switchToPureColorBackground(awtColor))
    }
  }

  private def onTransparentBackground(): Unit = {
    val enabled = transparentBackground.isSelected
    demoAppHolder.foreach(_.setTransparentBackground(enabled))
    DemoApp.saveTransparentBackground(enabled)
  }

  private def onDefaultBackgroundSelected(): Unit = {
    demoAppHolder.foreach(_.switchToDefaultBackground())
    transparentBackground.setSelected(false)
    DemoApp.saveTransparentBackground(false)
  }

  private def currentWindow: Window = Option(this.getScene).map(_.getWindow).orNull
}


