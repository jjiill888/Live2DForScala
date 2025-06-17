package moe.brianhsu.live2d.demo.javafx.widget

import javafx.scene.control.{Label, Tab, TabPane}
import javafx.scene.layout.VBox
import moe.brianhsu.live2d.demo.app.DemoApp

/**
 * Simple control panel with tabs similar to SWTAvatarControlPanel.
 * Functionality will be implemented later; currently this just
 * provides the tab structure.
 */
class JavaFXAvatarControlPanel extends VBox {

  private val tabPane = new TabPane()

  val normalTab: Tab = new Tab("Normal")
  val faceTrackingTab: Tab = new Tab("Face Tracking")
  val modelControlTab: Tab = new Tab("Model Control")

  normalTab.setClosable(false)
  faceTrackingTab.setClosable(false)
  modelControlTab.setClosable(false)

  private val normalBox = new VBox(new Label("Effects/Motions/Expressions coming soon"))
  private val faceTrackingBox = new VBox(new Label("Face tracking settings coming soon"))
  private val modelControlBox = new VBox(new Label("Model control coming soon"))

  normalTab.setContent(normalBox)
  faceTrackingTab.setContent(faceTrackingBox)
  modelControlTab.setContent(modelControlBox)

  tabPane.getTabs.addAll(normalTab, faceTrackingTab, modelControlTab)

  this.getChildren.add(tabPane)

  /**
   * Hook for wiring up the panel with DemoApp. Currently unused
   * until detailed controls are ported from the SWT version.
   */
  def setDemoApp(demoApp: DemoApp): Unit = {}
}

