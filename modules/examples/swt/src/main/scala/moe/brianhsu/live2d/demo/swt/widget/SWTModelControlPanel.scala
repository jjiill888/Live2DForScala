package moe.brianhsu.live2d.demo.swt.widget

import org.eclipse.swt.widgets.{Composite, TabFolder, TabItem, Button, FileDialog, Label, Text}
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.{GridLayout, GridData}
import org.json4s.native.JsonMethods.parse
import org.json4s.DefaultFormats
import scala.io.Source
import java.io.File

class ModelControlPanel(parent: Composite) extends Composite(parent, SWT.NONE) {

  private val tabFolder = new TabFolder(this, SWT.BORDER)

  // Constructor block
  {
    this.setLayout(new GridLayout(1, false))

    // Add Load JSON button
    val loadJsonButton = new Button(this, SWT.PUSH)
    loadJsonButton.setText("Load JSON")
    loadJsonButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false))

    loadJsonButton.addListener(SWT.Selection, _ => {
      val fileDialog = new FileDialog(getShell, SWT.OPEN)
      fileDialog.setText("Load JSON File")
      fileDialog.setFilterExtensions(Array("*.json"))
      val filePath = fileDialog.open()

      if (filePath != null) {
        loadJsonFromFile(filePath) match {
          case Some(modelData) =>
            println(s"Successfully loaded JSON file: $filePath")
            tabFolder.getItems.foreach(_.dispose()) // Clear existing tabs
            createPhysicsTabs(modelData)           // Create new tabs
            tabFolder.layout()
          case None =>
            println(s"Failed to load JSON file: $filePath")
        }
      }
    })

    // Attempt to load default JSON from def_avatar folder
    val defaultJsonPath = "def_avatar"
    val files = Option(new File(defaultJsonPath).listFiles).getOrElse(Array.empty)
    val defaultJsonFiles = files.filter(file => file.isFile && file.getName.endsWith("physics3.json"))

    if (defaultJsonFiles.isEmpty) {
      // Print warning and allow program to continue
      System.err.println("[WARN] No physics3.json files found in def_avatar. Skipping default model load.")
      new Label(this, SWT.NONE).setText("No default model data found.")
    } else {
      val defaultJsonFile = defaultJsonFiles.head
      loadJsonFromFile(defaultJsonFile.getAbsolutePath) match {
        case Some(modelData) =>
          println(s"Successfully loaded default JSON file: ${defaultJsonFile.getAbsolutePath}")
          createPhysicsTabs(modelData)
          this.layout()
        case None =>
          println(s"Failed to load default JSON file: ${defaultJsonFile.getAbsolutePath}")
          new Label(this, SWT.NONE).setText("Failed to load default model data.")
      }
    }

    tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))
  }

  // Load and parse JSON file
  private def loadJsonFromFile(filePath: String): Option[ModelData] = {
    try {
      val jsonContent = Source.fromFile(filePath, "UTF-8").mkString
      implicit val formats: DefaultFormats.type = DefaultFormats
      Some(parse(jsonContent).extract[ModelData])
    } catch {
      case ex: Exception =>
        println(s"Error loading JSON file: ${ex.getMessage}")
        None
    }
  }

  // Create tabs for physics settings
  private def createPhysicsTabs(modelData: ModelData): Unit = {
    val metaComposite = new Composite(tabFolder, SWT.NONE)
    metaComposite.setLayout(new GridLayout(2, false))
    val metaTab = new TabItem(tabFolder, SWT.NONE)
    metaTab.setText("Meta")
    metaTab.setControl(metaComposite)

    new Label(metaComposite, SWT.NONE).setText("Version:")
    val versionText = new Text(metaComposite, SWT.BORDER)
    versionText.setText(modelData.Version.toString)

    new Label(metaComposite, SWT.NONE).setText("FPS:")
    val fpsText = new Text(metaComposite, SWT.BORDER)
    fpsText.setText(modelData.Meta.Fps.toString)

    new Label(metaComposite, SWT.NONE).setText("Gravity:")
    val gravityText = new Text(metaComposite, SWT.BORDER)
    gravityText.setText(s"X: ${modelData.Meta.EffectiveForces.Gravity.X}, Y: ${modelData.Meta.EffectiveForces.Gravity.Y}")

    new Label(metaComposite, SWT.NONE).setText("Wind:")
    val windText = new Text(metaComposite, SWT.BORDER)
    windText.setText(s"X: ${modelData.Meta.EffectiveForces.Wind.X}, Y: ${modelData.Meta.EffectiveForces.Wind.Y}")

    if (modelData.Meta.PhysicsDictionary.isEmpty) {
      println("PhysicsDictionary is empty, no tabs to create.")
    } else {
      modelData.Meta.PhysicsDictionary.foreach { setting =>
        val tabItem = new TabItem(tabFolder, SWT.NONE)
        tabItem.setText(setting.Name)

        val composite = new Composite(tabFolder, SWT.NONE)
        composite.setLayout(new GridLayout(1, false))
        tabItem.setControl(composite)

        createPhysicsContent(composite, setting.Id, modelData)
      }
    }
    tabFolder.layout()
  }

  // Create UI content for a given physics setting
  private def createPhysicsContent(parent: Composite, settingId: String, modelData: ModelData): Unit = {
    val setting = modelData.PhysicsSettings.find(_.Id == settingId)

    setting.foreach { physicsSetting =>
      parent.setLayout(new GridLayout(2, false))

      // Input Parameters
      new Label(parent, SWT.NONE).setText("Input Parameters:")
      val inputComposite = new Composite(parent, SWT.NONE)
      inputComposite.setLayout(new GridLayout(2, false))
      physicsSetting.Input.foreach { input =>
        new Label(inputComposite, SWT.NONE).setText(s"Source: ${input.Source.Id}, Type: ${input.Type}")
        val weightText = new Text(inputComposite, SWT.BORDER)
        weightText.setText(input.Weight.toString)
      }

      // Output Parameters
      new Label(parent, SWT.NONE).setText("Output Parameters:")
      val outputComposite = new Composite(parent, SWT.NONE)
      outputComposite.setLayout(new GridLayout(2, false))
      physicsSetting.Output.foreach { output =>
        new Label(outputComposite, SWT.NONE).setText(s"Destination: ${output.Destination.Id}, Type: ${output.Type}")
        val scaleText = new Text(outputComposite, SWT.BORDER)
        scaleText.setText(output.Scale.toString)
      }

      // Vertices
      new Label(parent, SWT.NONE).setText("Vertices:")
      val vertexComposite = new Composite(parent, SWT.NONE)
      vertexComposite.setLayout(new GridLayout(4, false))
      physicsSetting.Vertices.foreach { vertex =>
        new Label(vertexComposite, SWT.NONE).setText(s"Position: (${vertex.Position.X}, ${vertex.Position.Y})")
        val mobilityText = new Text(vertexComposite, SWT.BORDER)
        mobilityText.setText(vertex.Mobility.toString)
      }

      // Normalization
      new Label(parent, SWT.NONE).setText("Normalization:")
      val normComposite = new Composite(parent, SWT.NONE)
      normComposite.setLayout(new GridLayout(2, false))
      new Label(normComposite, SWT.NONE).setText(s"Position:")
      val positionText = new Text(normComposite, SWT.BORDER)
      positionText.setText(s"Min=${physicsSetting.Normalization.Position.Minimum}, Max=${physicsSetting.Normalization.Position.Maximum}, Default=${physicsSetting.Normalization.Position.Default}")
      new Label(normComposite, SWT.NONE).setText(s"Angle:")
      val angleText = new Text(normComposite, SWT.BORDER)
      angleText.setText(s"Min=${physicsSetting.Normalization.Angle.Minimum}, Max=${physicsSetting.Normalization.Angle.Maximum}, Default=${physicsSetting.Normalization.Angle.Default}")
    }

    parent.layout()
  }
}

// JSON data model classes (unchanged) [omitted for brevity]
case class ModelData(
  Version: Int,
  Meta: MetaData,
  PhysicsSettings: List[PhysicsSetting]
)

case class MetaData(
  Fps: Int,
  EffectiveForces: EffectiveForces,
  PhysicsDictionary: List[PhysicsSettingInfo]
)

case class EffectiveForces(
  Gravity: Force,
  Wind: Force
)

case class Force(
  X: Double,
  Y: Double
)

case class PhysicsSettingInfo(
  Id: String,
  Name: String
)

case class PhysicsSetting(
  Id: String,
  Input: List[PhysicsInput],
  Output: List[PhysicsOutput],
  Vertices: List[Vertex],
  Normalization: Normalization
)

case class PhysicsInput(
  Source: SourceInfo,
  Weight: Double,
  Type: String,
  Reflect: Boolean
)

case class PhysicsOutput(
  Destination: DestinationInfo,
  VertexIndex: Int,
  Scale: Double,
  Weight: Double,
  Type: String,
  Reflect: Boolean
)

case class Vertex(
  Position: Position,
  Mobility: Double,
  Delay: Double,
  Acceleration: Double,
  Radius: Double
)

case class Normalization(
  Position: Range,
  Angle: Range
)

case class Range(
  Minimum: Double,
  Default: Double,
  Maximum: Double
)

case class SourceInfo(
  Id: String
)

case class DestinationInfo(
  Id: String
)

case class Position(
  X: Double,
  Y: Double
)
