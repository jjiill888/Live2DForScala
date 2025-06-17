package moe.brianhsu.live2d.demo.javafx

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea.AvatarListener
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXToolbar

/** Simple JavaFX entry point that will later host the Live2D view. */
class JavaFXMain extends Application {
  override def start(primaryStage: Stage): Unit = {
    val avatarArea = new JavaFXAvatarDisplayArea
    val toolbar = new JavaFXToolbar

    avatarArea.setAvatarListener(new AvatarListener {
      override def onAvatarLoaded(live2DView: DemoApp): Unit = {}
      override def onStatusUpdated(status: String): Unit = {}
    })

    avatarArea.onDemoAppReady { app =>
      toolbar.setDemoApp(app)
      DemoApp.loadLastAvatarPath() match {
        case Some(path) =>
          app.switchAvatar(path).recoverWith { case _ => app.switchAvatar("def_avatar") }
        case None =>
          app.switchAvatar("def_avatar")
      }
    }

    val root = new BorderPane()
    root.setTop(toolbar)
    root.setCenter(avatarArea)
    val scene = new Scene(root, 800, 600)
    scene.getStylesheets.add(getClass.getResource("/style/dark-theme.css").toExternalForm)
    primaryStage.setTitle("Live2D Scala Demo (JavaFX)")
    primaryStage.setScene(scene)
    primaryStage.show()
  }
}

object JavaFXMain {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[JavaFXMain], args: _*)
  }
}