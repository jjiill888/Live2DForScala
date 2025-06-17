package moe.brianhsu.live2d.demo.javafx.widget

import javafx.scene.control.Label
import javafx.scene.layout.HBox

/** Simple status bar for JavaFX demo to show loading messages. */
class JavaFXStatusBar extends HBox {
  private val statusLabel = new Label("Ready.")
  this.getChildren.add(statusLabel)

  def updateStatus(message: String): Unit = {
    statusLabel.setText(message)
  }
}