package moe.brianhsu.live2d.demo.swt.widget.faceTracking

import moe.brianhsu.live2d.demo.app.LanguageManager
import moe.brianhsu.live2d.demo.openSeeFace.{CameraListing, OpenSeeFaceSetting}
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets.{Button, Combo, Composite, Label, Text}

class SWTOpenSeeFaceBundle(parent: Composite, cameraListing: CameraListing) extends Composite(parent, SWT.NONE) with OpenSeeFaceSetting {
  private val cameraCombo = createComboField(this, LanguageManager.getText("openseeface.camera"), cameraListing.listing.map(_.title), 0, LanguageManager.getText("openseeface.camera_tooltip"))
  val webcamResetButton = createResetButton(this)
  private val fpsCombo = createComboField(
    this, LanguageManager.getText("openseeface.fps"), List("24", "30", "60", "120"), 2,
    LanguageManager.getText("openseeface.fps_tooltip")
  )

  private val resolutionCombo = createComboField(
    this, LanguageManager.getText("openseeface.resolution"), List("320x240", "640x480", "1024x768", "1280x720", "1920x1080"), 1,
    LanguageManager.getText("openseeface.resolution_tooltip")
  )

  private val portText = createTextField(this, LanguageManager.getText("openseeface.port"), "11573", LanguageManager.getText("openseeface.port_tooltip"))

  private val modelCombo = createComboField(
    this, LanguageManager.getText("openseeface.model"), List("-3", "-2", "-1", "0", "1", "2", "3", "4"), 7,
    LanguageManager.getText("openseeface.model_tooltip")
  )
  private val visualizeCombo = createComboField(
    this, LanguageManager.getText("openseeface.visualize"), List("0", "1", "2", "3", "4"), 0,
    LanguageManager.getText("openseeface.visualize_tooltip")
  )

  {
    this.setLayout(new GridLayout(2, true))
  }

  override def getCommand: String = {
    s"${OpenSeeFaceSetting.bundleExecution} " +
      s"--model-dir ${OpenSeeFaceSetting.bundleModelDir} -M " +
      cameraIdSetting +
      cameraResolutionSetting +
      Option(portText.getText).filter(_.nonEmpty).map("--port " + _ + " ").getOrElse("") +
      Option(fpsCombo.getText).filter(_.nonEmpty).map("--fps " + _ + " ").getOrElse("") +
      Option(modelCombo.getText).filter(_.nonEmpty).map("--model " + _ + " ").getOrElse("") +
      Option(visualizeCombo.getText).filter(_.nonEmpty).map("--visualize " + _ + " ").getOrElse("")
  }

  private def cameraResolutionSetting: String = {
    val Array(width, height) = resolutionCombo.getText.split("x")
    s"--width $width --height $height "
  }

  private def cameraIdSetting: String = {
    val cameraId = cameraListing.listing
      .find(_.title == cameraCombo.getText)
      .map(_.cameraId)
      .getOrElse(0)

    s"--capture $cameraId "
  }

  private def createResetButton(parent: Composite): Button = {
    val button = new Button(parent, SWT.PUSH)
    button.setText("Webcam reset")

    val gridData = new GridData
    gridData.horizontalAlignment = GridData.END
    gridData.grabExcessHorizontalSpace = true
    gridData.horizontalSpan = 2
    button.setLayoutData(gridData)

    button
  }
  
  private def createComboField(parent: Composite, title: String, values: List[String], defaultIndex: Int, tooltip:String): Combo = {
    val titleLabel = new Label(parent, SWT.NONE)
    val comboBox = new Combo(parent, SWT.DROP_DOWN|SWT.READ_ONLY)

    titleLabel.setText(title)
    comboBox.setItems(values: _*)
    comboBox.select(defaultIndex)
    comboBox.setToolTipText(tooltip)

    val gridData = new GridData
    gridData.horizontalAlignment = GridData.FILL
    gridData.grabExcessHorizontalSpace = true
    comboBox.setLayoutData(gridData)

    comboBox
  }

  private def createTextField(parent: Composite, title: String, default: String, tooltip: String = ""): Text = {
    val titleLabel = new Label(parent, SWT.NONE)
    val textField = new Text(parent, SWT.BORDER | SWT.SINGLE)

    titleLabel.setText(title)
    textField.setText(default)
    textField.setToolTipText(tooltip)

    val gridData = new GridData
    gridData.horizontalAlignment = GridData.FILL
    gridData.grabExcessHorizontalSpace = true
    textField.setLayoutData(gridData)

    textField
  }

  override def getHostname: String = "127.0.0.1"

  override def getPort: Int = portText.getText.toInt
}
