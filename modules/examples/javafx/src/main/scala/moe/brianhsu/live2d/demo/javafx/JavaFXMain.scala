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
          app.switchAvatar(path).recoverWith { case _ => app.switchAvatar("def_avatar") }
        case None =>
          app.switchAvatar("def_avatar")
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
    primaryStage.show()
  }

  private def loadInitialAvatar(app: DemoApp): Unit = {
    val loadResult = DemoApp.loadLastAvatarPath() match {
      case Some(path) =>
        app.switchAvatar(path).recoverWith { case _ => app.switchAvatar("def_avatar") }
      case None =>
        app.switchAvatar("def_avatar")
    }
    loadResult.failed.foreach(e => System.err.println(s"[WARN] Cannot load default avatar: ${e.getMessage}"))
  }
}

object JavaFXMain {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[JavaFXMain], args: _*)
  }
}