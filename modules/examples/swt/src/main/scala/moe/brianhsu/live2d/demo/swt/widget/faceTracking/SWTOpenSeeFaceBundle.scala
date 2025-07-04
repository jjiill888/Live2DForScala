package moe.brianhsu.live2d.demo.swt.widget.faceTracking

import moe.brianhsu.live2d.demo.openSeeFace.{CameraListing, OpenSeeFaceSetting}
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets.{Button, Combo, Composite, Label, Text}

class SWTOpenSeeFaceBundle(parent: Composite, cameraListing: CameraListing) extends Composite(parent, SWT.NONE) with OpenSeeFaceSetting {
  private val cameraCombo = createComboField(this, "Camera:", cameraListing.listing.map(_.title), 0, "Select camera for face tracking")
  val webcamResetButton = createResetButton(this)
  private val fpsCombo = createComboField(
    this, "FPS:", List("24", "30", "60", "120"), 2,
    "Set camera frames per second"
  )

  private val resolutionCombo = createComboField(
    this, "Resolution:", List("320x240", "640x480", "1024x768", "1280x720", "1920x1080"), 1,
    "Set camera resolution"
  )

  private val portText = createTextField(this, "Port:", "11573", "Set port for sending tracking data")

  private val modelCombo = createComboField(
    this, "Model:", List("-3", "-2", "-1", "0", "1", "2", "3", "4"), 7,
    "This can be used to select the tracking model.\n" +
      "Higher numbers are models with better tracking quality, but slower speed, " +
      "except for model 4, which is wink optimized.\n" +
      "Models 1 and 0 tend to be too rigid for expression and blink detection.\n" +
      "Model -2 is roughly equivalent to model 1, but faster.\n " +
      "Model -3 is between models 0 and -1."
  )
  private val visualizeCombo = createComboField(
    this, "Visualize:", List("0", "1", "2", "3", "4"), 0,
    "Set this to 1 to visualize the tracking, to 2 to also show face ids, " +
      "to 3 to add confidence values or to 4 to add numbers to the point display."
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
