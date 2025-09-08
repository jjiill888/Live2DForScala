package moe.brianhsu.live2d.demo.swt.widget.faceTracking

import moe.brianhsu.live2d.demo.app.LanguageManager
import moe.brianhsu.live2d.demo.openSeeFace.OpenSeeFaceSetting
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets._

import scala.annotation.unused

class SWTOpenSeeFaceAdvance(parent: Composite) extends Composite(parent, SWT.NONE) with OpenSeeFaceSetting {

  private val commandText = createTextField(this, LanguageManager.getText("openseeface.command"), "python facetracker.py")
  private val ipText = createTextField(this, LanguageManager.getText("openseeface.ip"), "127.0.0.1", LanguageManager.getText("openseeface.ip_tooltip"))
  private val portText = createTextField(this, LanguageManager.getText("openseeface.port"), "11573", LanguageManager.getText("openseeface.port_tooltip"))
  private val cameraIdText = createTextField(this, LanguageManager.getText("openseeface.camera_id"), "0", LanguageManager.getText("openseeface.camera_id_tooltip"))
  private val modelCombo = createComboField(
    this, LanguageManager.getText("openseeface.model"), Array("-3", "-2", "-1", "0", "1", "2", "3", "4"), 7,
    LanguageManager.getText("openseeface.model_tooltip")
  )
  private val visualizeCombo = createComboField(
    this, LanguageManager.getText("openseeface.visualize"), Array("0", "1", "2", "3", "4"), 0,
    LanguageManager.getText("openseeface.visualize_tooltip")
  )
  private val extraParameterText = createTextField(this, LanguageManager.getText("openseeface.extra_parameters"), "--width 640 --height 480 --fps 60")
  private val mirrorCheckbox = createCheckbox(this, LanguageManager.getText("openseeface.mirror_input"), LanguageManager.getText("openseeface.mirror_input_tooltip"))
  private val commandPreviewText = createCommandPreview(this, LanguageManager.getText("openseeface.command_preview"))

  {
    this.setLayout(new GridLayout(2, true))

    commandText.addListener(SWT.Modify, { (event: Event) => updateSegments(event) })
    ipText.addListener(SWT.Modify, { (event: Event) => updateSegments(event) })
    portText.addListener(SWT.Modify, { (event: Event) => updateSegments(event) })
    cameraIdText.addListener(SWT.Modify, { (event: Event) => updateSegments(event) })
    modelCombo.addListener(SWT.Selection, { (event: Event) => updateSegments(event) })
    visualizeCombo.addListener(SWT.Selection, { (event: Event) => updateSegments(event) })
    extraParameterText.addListener(SWT.Modify, { (event: Event) => updateSegments(event) })
    mirrorCheckbox.addListener(SWT.Selection, { (event: Event) => updateSegments(event) })

    updateCommandPreview()
  }

  override def getCommand: String = commandPreviewText.getText

  private def updateSegments(event: Event): Unit = {
    updateCommandPreview()
  }

  private def updateCommandPreview(): Unit = {
    val command =
      commandText.getText + " " +
      Option(ipText.getText).filter(_.nonEmpty).map("--ip " + _ + " ").getOrElse("") +
      Option(portText.getText).filter(_.nonEmpty).map("--port " + _ + " ").getOrElse("") +
      Option(cameraIdText.getText).filter(_.nonEmpty).map("--capture " + _ + " ").getOrElse("") +
      Option(modelCombo.getText).filter(_.nonEmpty).map("--model " + _ + " ").getOrElse("") +
      Option(visualizeCombo.getText).filter(_.nonEmpty).map("--visualize " + _ + " ").getOrElse("") +
      (if (mirrorCheckbox.getSelection) { "--mirror-input " } else {""}) +
      Option(extraParameterText.getText).filter(_.nonEmpty).map(_ + " ").getOrElse("")

    commandPreviewText.setText(command)
  }

  private def createCommandPreview(parent: Composite, title: String): Text = {
    val titleLabel = new Label(parent, SWT.NONE)
    val previewText = new Text(parent, SWT.BORDER|SWT.WRAP|SWT.MULTI|SWT.V_SCROLL|SWT.READ_ONLY)

    val gridData = new GridData
    gridData.horizontalAlignment = GridData.FILL
    gridData.grabExcessHorizontalSpace = true
    gridData.horizontalSpan = 2
    titleLabel.setText(title)
    titleLabel.setLayoutData(gridData)

    val gd = new GridData()
    gd.horizontalAlignment = GridData.FILL
    gd.grabExcessHorizontalSpace = true
    gd.horizontalSpan = 2
    gd.heightHint = previewText.getLineHeight * 3

    previewText.setLayoutData(gd)

    previewText
  }

  private def createCheckbox(parent: Composite, title: String, tooltip: String): Button = {
    val checkbox = new Button(parent, SWT.CHECK)

    checkbox.setText(title)
    checkbox.setToolTipText(tooltip)
    checkbox.setSelection(true)

    val gridData = new GridData
    gridData.horizontalAlignment = GridData.FILL
    gridData.grabExcessHorizontalSpace = true
    gridData.horizontalSpan = 2
    checkbox.setLayoutData(gridData)

    checkbox
  }

  private def createComboField(parent: Composite, title: String, values: Array[String], defaultIndex: Int, tooltip:String): Combo = {
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

  override def getHostname: String = ipText.getText

  override def getPort: Int = portText.getText.toInt
}
