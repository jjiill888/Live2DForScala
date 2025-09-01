package moe.brianhsu.live2d.demo.swt

import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.swt.widget.SWTAvatarDisplayArea.AvatarListener
import moe.brianhsu.live2d.demo.swt.widget.{SWTAvatarControlPanel, SWTAvatarDisplayArea, SWTStatusBar, SWTToolbar}
import moe.brianhsu.live2d.adapter.util.WaylandSupport
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets.{Display, Shell, Listener, Event, Button, Composite}

/**
 * 专门用于测试Clipping Mask FBO修复的SWT示例程序
 * 这个程序会持续渲染Live2D模型，以验证Clipping Mask是否正常工作
 */
object ClippingMaskTest {

  WaylandSupport.setup()
  
  private val display = new Display()
  private val shell = new Shell(display)
  private val toolbar = new SWTToolbar(shell)
  private val sashForm = new SashForm(shell, SWT.HORIZONTAL|SWT.SMOOTH)
  private val avatarControl = new SWTAvatarControlPanel(sashForm)
  private val avatarArea = new SWTAvatarDisplayArea(sashForm)
  private val statusBar = new SWTStatusBar(shell)
  
      // Add test control panel
  private val testPanel = new ClippingMaskTestPanel(shell)

  def main(args: Array[String]): Unit = {
    setupUILayout()
    setupAvatarEventListener()
    setupTestControls()

    shell.setText("Live 2D Clipping Mask FBO Test (SWT+LWJGL)")
    
    shell.open()
    
    display.asyncExec(() => loadInitialAvatar())

    // Main loop
    while (!shell.isDisposed) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }
    display.dispose()
    System.exit(0)
  }

  private def setupTestControls(): Unit = {
    testPanel.setDemoApp(avatarArea.demoApp)
  }

  private def setupAvatarEventListener(): Unit = {
    avatarControl.setDemoApp(avatarArea.demoApp)
    toolbar.setDemoApp(avatarArea.demoApp)
    avatarArea.setAvatarListener(new AvatarListener {
      override def onAvatarLoaded(live2DView: DemoApp): Unit = {
        live2DView.avatarHolder.foreach(avatarControl.expressionSelector.updateExpressionList)
        live2DView.avatarHolder.foreach(avatarControl.motionSelector.updateMotionTree)
        live2DView.strategyHolder.foreach { strategy =>
          avatarControl.effectSelector.syncWithStrategy(strategy)
          avatarControl.motionSelector.syncWithStrategy(strategy)
        }
        avatarControl.faceTrackingComposite.enableStartButton()
        statusBar.updateStatus("Avatar loaded successfully - Clipping Mask FBO test ready")
      }
      override def onStatusUpdated(status: String): Unit = {
        statusBar.updateStatus(status)
      }
    })
  }

  private def setupUILayout(): Unit = {
    shell.setLayout(new GridLayout(1, false))
    sashForm.setWeights(1, 4)
    sashForm.setSashWidth(5)

    val gridData = new GridData
    gridData.horizontalAlignment = GridData.FILL
    gridData.grabExcessHorizontalSpace = true
    toolbar.setLayoutData(gridData)

    val gridData1 = new GridData
    gridData1.horizontalAlignment = GridData.FILL
    gridData1.verticalAlignment = GridData.FILL
    gridData1.grabExcessHorizontalSpace = true
    gridData1.grabExcessVerticalSpace = true
    sashForm.setLayoutData(gridData1)

    val gridData2 = new GridData
    gridData2.horizontalAlignment = GridData.FILL
    gridData2.grabExcessHorizontalSpace = true
    testPanel.setLayoutData(gridData2)

    val gridData4 = new GridData
    gridData4.horizontalAlignment = GridData.FILL
    gridData4.grabExcessHorizontalSpace = true
    statusBar.setLayoutData(gridData4)
  }

  private def loadInitialAvatar(): Unit = {
    val loadResult = DemoApp.loadLastAvatarPath() match {
      case Some(path) =>
        avatarArea.demoApp.switchAvatar(path).recoverWith { case e =>
          System.err.println(s"[WARN] Cannot load last avatar '$path': ${e.getMessage}")
          avatarArea.demoApp.switchAvatar("def_avatar")
        }
      case None =>
        avatarArea.demoApp.switchAvatar("def_avatar")
    }
    
    loadResult.foreach { _ =>
      statusBar.updateStatus("Default avatar loaded for Clipping Mask FBO testing")
    }
  }
}

/**
 * Clipping Mask测试控制面板
 */
class ClippingMaskTestPanel(parent: Composite) extends Composite(parent, SWT.NONE) {
  
  private var demoApp: Option[DemoApp] = None
  
  setLayout(new GridLayout(3, false))
  
  private val startTestButton = new Button(this, SWT.PUSH)
  startTestButton.setText("Start Clipping Mask Test")
  startTestButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
  private val stressTestButton = new Button(this, SWT.PUSH)
  stressTestButton.setText("Stress Test (5 min)")
  stressTestButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
  private val statusLabel = new Button(this, SWT.PUSH)
  statusLabel.setText("Test Status: Ready")
  statusLabel.setEnabled(false)
  statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
  startTestButton.addListener(SWT.Selection, _ => startClippingMaskTest())
  stressTestButton.addListener(SWT.Selection, _ => startStressTest())
  
  def setDemoApp(app: DemoApp): Unit = {
    demoApp = Some(app)
    startTestButton.setEnabled(true)
    stressTestButton.setEnabled(true)
  }
  
  private def startClippingMaskTest(): Unit = {
    demoApp.foreach { app =>
      statusLabel.setText("Test Status: Running...")
      // Add specific test logic here
      // For example, continuously switch expressions and actions to test Clipping Mask
    }
  }
  
  private def startStressTest(): Unit = {
    demoApp.foreach { app =>
      statusLabel.setText("Test Status: Stress Test Running...")
          // Implement 5-minute continuous rendering test
    // Verify that Clipping Mask FBO remains normal after long-term operation
    }
  }
}
