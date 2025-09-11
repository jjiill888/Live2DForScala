package moe.brianhsu.live2d.demo.javafx

import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.control.{Button, Label, MenuBar, Menu, MenuItem}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{BorderPane, VBox, HBox}
import javafx.stage.{FileChooser, Stage}
import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea.AvatarListener
import scala.util.{Success, Failure}
import java.io.File

class JavaFXMain extends Application {
  private var avatarDisplayArea: JavaFXAvatarDisplayArea = _
  private var statusLabel: Label = _
  private var isUIHidden = false
  private var controlPane: VBox = _
  private var menuBar: MenuBar = _

  override def start(primaryStage: Stage): Unit = {
    // Initialize components
    avatarDisplayArea = new JavaFXAvatarDisplayArea()
    statusLabel = new Label("Ready to load Live2D model...")
    
    setupUI(primaryStage)
    setupEventHandlers(primaryStage)
    
    primaryStage.setTitle("Live2D For Scala - JavaFX + LWJGL")
    primaryStage.setWidth(1080)
    primaryStage.setHeight(720)
    primaryStage.show()

    // Initialize OpenGL after the stage is shown
    Platform.runLater(() => {
      avatarDisplayArea.initializeOpenGL()
      loadInitialModel()
    })
  }

  private def setupUI(primaryStage: Stage): Unit = {
    val root = new BorderPane()
    
    // Menu Bar
    menuBar = createMenuBar(primaryStage)
    
    // Control panel
    controlPane = createControlPane(primaryStage)
    
    // Status bar
    val statusBox = new HBox()
    statusBox.getChildren.add(statusLabel)
    statusBox.setStyle("-fx-padding: 5px; -fx-border-color: gray; -fx-border-width: 1px 0 0 0;")
    
    root.setTop(menuBar)
    root.setLeft(controlPane)
    root.setCenter(avatarDisplayArea)
    root.setBottom(statusBox)
    
    val scene = new Scene(root)
    primaryStage.setScene(scene)
    
    // Setup avatar event listener
    avatarDisplayArea.setAvatarListener(new AvatarListener {
      override def onAvatarLoaded(live2DView: DemoApp): Unit = {
        Platform.runLater(() => {
          statusLabel.setText("Live2D model loaded successfully!")
        })
      }
      
      override def onStatusUpdated(status: String): Unit = {
        Platform.runLater(() => {
          statusLabel.setText(status)
        })
      }
    })
  }

  private def createMenuBar(primaryStage: Stage): MenuBar = {
    val menuBar = new MenuBar()
    
    val fileMenu = new Menu("File")
    val loadMenuItem = new MenuItem("Load Avatar...")
    loadMenuItem.setOnAction(_ => loadAvatarFile(primaryStage))
    val exitMenuItem = new MenuItem("Exit")
    exitMenuItem.setOnAction(_ => Platform.exit())
    
    fileMenu.getItems.addAll(loadMenuItem, exitMenuItem)
    
    val viewMenu = new Menu("View")
    val toggleUIMenuItem = new MenuItem("Toggle UI (ESC)")
    toggleUIMenuItem.setOnAction(_ => toggleUI())
    
    viewMenu.getItems.add(toggleUIMenuItem)
    
    menuBar.getMenus.addAll(fileMenu, viewMenu)
    menuBar
  }

  private def createControlPane(primaryStage: Stage): VBox = {
    val controlPane = new VBox(10)
    controlPane.setStyle("-fx-padding: 10px; -fx-border-color: gray; -fx-border-width: 0 1px 0 0; -fx-min-width: 200px;")
    
    val loadButton = new Button("Load Avatar...")
    loadButton.setOnAction(_ => loadAvatarFile(primaryStage))
    loadButton.setPrefWidth(180)
    
    val infoLabel = new Label("Controls:")
    infoLabel.setStyle("-fx-font-weight: bold;")
    
    val controlsInfo = new Label(
      """Left click: Select
        |Right drag: Move model
        |Mouse wheel: Zoom
        |ESC: Toggle UI
        |1-9: Expressions""".stripMargin)
    controlsInfo.setStyle("-fx-font-size: 12px;")
    
    controlPane.getChildren.addAll(loadButton, infoLabel, controlsInfo)
    controlPane
  }

  private def setupEventHandlers(primaryStage: Stage): Unit = {
    // ESC key handler
    primaryStage.getScene.setOnKeyPressed((event: KeyEvent) => {
      if (event.getCode == KeyCode.ESCAPE) {
        toggleUI()
      }
    })
    
    // Cleanup on close
    primaryStage.setOnCloseRequest(_ => {
      avatarDisplayArea.cleanup()
      Platform.exit()
    })
  }

  private def loadAvatarFile(primaryStage: Stage): Unit = {
    val fileChooser = new FileChooser()
    fileChooser.setTitle("Select Live2D Model File")
    fileChooser.getExtensionFilters.addAll(
      new FileChooser.ExtensionFilter("Live2D Model Files", "*.model3.json"),
      new FileChooser.ExtensionFilter("All Files", "*.*")
    )
    
    // Set initial directory to the 4.1a model directory if it exists
    val modelDir = new File("4.1a")
    if (modelDir.exists() && modelDir.isDirectory) {
      fileChooser.setInitialDirectory(modelDir)
    }
    
    val selectedFile = fileChooser.showOpenDialog(primaryStage)
    if (selectedFile != null) {
      loadAvatar(selectedFile.getAbsolutePath)
    }
  }

  private def loadAvatar(path: String): Unit = {
    statusLabel.setText("Loading avatar...")
    
    // Load avatar in background thread
    val loadTask = new javafx.concurrent.Task[Unit] {
      override def call(): Unit = {
        try {
          avatarDisplayArea.demoApp.switchAvatar(path) match {
            case Success(_) =>
              Platform.runLater(() => statusLabel.setText(s"Loaded: ${new File(path).getName}"))
            case Failure(exception) =>
              Platform.runLater(() => statusLabel.setText(s"Failed to load avatar: ${exception.getMessage}"))
          }
        } catch {
          case e: Exception =>
            Platform.runLater(() => statusLabel.setText(s"DemoApp not ready: ${e.getMessage}"))
        }
      }
    }
    
    val thread = new Thread(loadTask)
    thread.setDaemon(true)
    thread.start()
  }

  private def loadInitialModel(): Unit = {
    // Try to load the 4.1a model if it exists
    val modelPath = "4.1a/spl2.model3.json"
    val modelFile = new File(modelPath)
    
    if (modelFile.exists()) {
      loadAvatar(modelPath)
    } else {
      statusLabel.setText("Ready - Use 'Load Avatar...' to load a Live2D model")
    }
  }

  private def toggleUI(): Unit = {
    if (isUIHidden) {
      showUI()
    } else {
      hideUI()
    }
  }

  private def hideUI(): Unit = {
    isUIHidden = true
    menuBar.setVisible(false)
    menuBar.setManaged(false)
    controlPane.setVisible(false)
    controlPane.setManaged(false)
  }

  private def showUI(): Unit = {
    isUIHidden = false
    menuBar.setVisible(true)
    menuBar.setManaged(true)
    controlPane.setVisible(true)
    controlPane.setManaged(true)
  }
}

object JavaFXMain {
  def main(args: Array[String]): Unit = {
    // Set JavaFX system properties
    System.setProperty("javafx.animation.pulse", "60")
    System.setProperty("prism.vsync", "true")
    
    Application.launch(classOf[JavaFXMain], args: _*)
  }
}