import javafx.application.Application
import javafx.scene.{Scene}
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

/** Simple JavaFX entry point that will later host the Live2D view. */
class JavaFXMain extends Application {
  override def start(primaryStage: Stage): Unit = {
    val root = new BorderPane()
    val scene = new Scene(root, 800, 600)
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

