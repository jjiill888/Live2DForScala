package moe.brianhsu.live2d.demo.swt.widget

import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.swt.widget.ModelControlPanel
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets._

class SWTAvatarControlPanel(parent: Composite) extends Composite(parent, SWT.NONE) {

  val tabFolder = new TabFolder(this, SWT.BORDER)

  val tabItemNormal = new TabItem(tabFolder, SWT.NONE)
  val tabItemFaceTracking = new TabItem(tabFolder, SWT.NONE)
  val tabItemModelControl = new TabItem(tabFolder, SWT.NONE)

  tabItemNormal.setText("Normal")
  tabItemFaceTracking.setText("Face Tracking")
  tabItemModelControl.setText("Model Control")

  val normalComposite = new Composite(tabFolder, SWT.NONE)
  val faceTrackingComposite = new SWTFaceTrackingComposite(tabFolder)
  val modelControlPanel = new ModelControlPanel(tabFolder) // Create ModelControlPanel instance

  tabItemNormal.setControl(normalComposite)
  tabItemFaceTracking.setControl(faceTrackingComposite)
  tabItemModelControl.setControl(modelControlPanel) // Add ModelControlPanel to the tab

  val effectSelector = new SWTEffectSelector(normalComposite)
  val motionSelector = new SWTMotionSelector(normalComposite)
  val expressionSelector = new SWTExpressionSelector(normalComposite)

  {
    this.setLayout(new FillLayout(SWT.VERTICAL))
    val fillLayout = new FillLayout(SWT.VERTICAL)
    fillLayout.marginWidth = 10
    this.normalComposite.setLayout(fillLayout)
  }

  def setDemoApp(demoApp: DemoApp): Unit = {
    effectSelector.setDemoApp(demoApp)
    motionSelector.setDemoApp(demoApp)
    expressionSelector.setDemoApp(demoApp)
    faceTrackingComposite.setDemoApp(demoApp)
    modelControlPanel.setDemoApp(Some(demoApp))
    
    // Start real-time parameter refresh for face tracking integration
    modelControlPanel.startRealTimeRefresh()
  }
}
