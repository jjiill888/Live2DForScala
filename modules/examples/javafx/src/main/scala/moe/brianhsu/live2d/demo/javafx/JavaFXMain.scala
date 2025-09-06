package moe.brianhsu.live2d.demo.javafx

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.geometry.Orientation
import javafx.stage.Stage
import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea.AvatarListener
import moe.brianhsu.live2d.demo.javafx.widget.{JavaFXAvatarControlPanel, JavaFXStatusBar, JavaFXToolbar}

/** Simple JavaFX entry point that will later host the Live2D view. */
class JavaFXMain extends Application {
  override def start(primaryStage: Stage): Unit = {
    // Load window settings on startup
    loadWindowSettings(primaryStage)
    
    val avatarArea = new JavaFXAvatarDisplayArea
    val toolbar = new JavaFXToolbar
    val controlPanel = new JavaFXAvatarControlPanel
    val statusBar = new JavaFXStatusBar

    avatarArea.setAvatarListener(new AvatarListener {
      override def onAvatarLoaded(live2DView: DemoApp): Unit =
        statusBar.updateStatus("Avatar loaded")
      override def onStatusUpdated(status: String): Unit =
        statusBar.updateStatus(status)
    })

    avatarArea.onDemoAppReady { app =>
      toolbar.setDemoApp(app)
      controlPanel.setDemoApp(app)
      DemoApp.loadLastAvatarPath() match {
        case Some(path) =>
          app.switchAvatar(path).recoverWith { case e =>
            System.err.println(s"[WARN] Cannot load last avatar '$path': ${e.getMessage}")
            // 不再尝试加载默认avatar
            scala.util.Failure(e)
          }
        case None =>
          // 不再尝试加载默认avatar
          System.out.println("[INFO] No avatar to load on startup. Please use 'Load Avatar' button to load a model.")
      }
      loadInitialAvatar(app)
    }

    val splitPane = new SplitPane()
    splitPane.setOrientation(Orientation.HORIZONTAL)
    splitPane.getItems.addAll(controlPanel, avatarArea)
    splitPane.setDividerPositions(0.2)

    val root = new BorderPane()
    root.setTop(toolbar)
    root.setCenter(avatarArea)
    root.setCenter(splitPane)
    root.setBottom(statusBar)
    val scene = new Scene(root, 800, 600)
    scene.getStylesheets.add(getClass.getResource("/style/dark-theme.css").toExternalForm)
    primaryStage.setTitle("Live2D Scala Demo (JavaFX)")
    primaryStage.setScene(scene)
    
    // Add window close listener to save settings
    primaryStage.setOnCloseRequest(_ => saveWindowSettings(primaryStage))
    
    primaryStage.show()
  }

  private def loadInitialAvatar(app: DemoApp): Unit = {
    val loadResult = DemoApp.loadLastAvatarPath() match {
      case Some(path) =>
        app.switchAvatar(path).recoverWith { case e =>
          System.err.println(s"[WARN] Cannot load last avatar '$path': ${e.getMessage}")
          // No longer attempt to load default avatar
          scala.util.Failure(e)
        }
      case None =>
        // No longer attempt to load default avatar
        System.out.println("[INFO] No avatar to load on startup. Please use 'Load Avatar' button to load a model.")
        scala.util.Success(())
    }
    loadResult.failed.foreach(e => System.err.println(s"[WARN] Cannot load avatar: ${e.getMessage}"))
  }

  // Load window settings from saved configuration
  private def loadWindowSettings(stage: Stage): Unit = {
    import moe.brianhsu.live2d.demo.app.DemoApp.loadWindowSettings
    loadWindowSettings() match {
      case Some((x, y, width, height, maximized)) =>
        stage.setX(x)
        stage.setY(y)
        stage.setWidth(width)
        stage.setHeight(height)
        if (maximized) {
          stage.setMaximized(true)
        }
      case None =>
        // Use default size if no settings found
        stage.setWidth(800)
        stage.setHeight(600)
        stage.centerOnScreen()
    }
  }

  // Save current window settings
  private def saveWindowSettings(stage: Stage): Unit = {
    import moe.brianhsu.live2d.demo.app.DemoApp.saveWindowSettings
    val x = stage.getX.toInt
    val y = stage.getY.toInt
    val width = stage.getWidth.toInt
    val height = stage.getHeight.toInt
    val maximized = stage.isMaximized
    saveWindowSettings(x, y, width, height, maximized)
  }
}

object JavaFXMain {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[JavaFXMain], args: _*)
  }
}