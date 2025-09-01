package moe.brianhsu.live2d.demo.swt

import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.swt.widget.SWTAvatarDisplayArea.AvatarListener
import moe.brianhsu.live2d.demo.swt.widget.{SWTAvatarControlPanel, SWTAvatarDisplayArea, SWTStatusBar, SWTToolbar}
import moe.brianhsu.live2d.adapter.util.WaylandSupport
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets.{Display, Shell, Button, Composite, Label, Text}

/**
 * 专门用于测试preDraw/postDraw流程的SWT示例程序
 * 这个程序会持续渲染Live2D模型，验证OpenGL状态管理是否正确
 */
object PreDrawPostDrawTest {

  WaylandSupport.setup()
  
  private val display = new Display()
  private val shell = new Shell(display)
  private val toolbar = new SWTToolbar(shell)
  private val sashForm = new SashForm(shell, SWT.HORIZONTAL|SWT.SMOOTH)
  private val avatarControl = new SWTAvatarControlPanel(sashForm)
  private val avatarArea = new SWTAvatarDisplayArea(sashForm)
  private val statusBar = new SWTStatusBar(shell)
  
      // Add test control panel
  private val testPanel = new PreDrawPostDrawTestPanel(shell)

  def main(args: Array[String]): Unit = {
    setupUILayout()
    setupAvatarEventListener()
    setupTestControls()

    shell.setText("Live 2D PreDraw/PostDraw Test (SWT+LWJGL)")
    
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
        statusBar.updateStatus("Avatar loaded successfully - PreDraw/PostDraw test ready")
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
      statusBar.updateStatus("Default avatar loaded for PreDraw/PostDraw testing")
    }
  }
}

/**
 * PreDraw/PostDraw测试控制面板
 */
class PreDrawPostDrawTestPanel(parent: Composite) extends Composite(parent, SWT.NONE) {
  
  private var demoApp: Option[DemoApp] = None
  
  setLayout(new GridLayout(2, false))
  
      // Test control buttons
  private val startTestButton = new Button(this, SWT.PUSH)
  startTestButton.setText("Start PreDraw/PostDraw Test")
  startTestButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
  private val stressTestButton = new Button(this, SWT.PUSH)
  stressTestButton.setText("Stress Test (10 min)")
  stressTestButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
      // Status display
  new Label(this, SWT.NONE).setText("Test Status:")
  private val statusText = new Text(this, SWT.READ_ONLY | SWT.BORDER)
  statusText.setText("Ready")
  statusText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
      // Test result statistics
  new Label(this, SWT.NONE).setText("Render Count:")
  private val renderCountText = new Text(this, SWT.READ_ONLY | SWT.BORDER)
  renderCountText.setText("0")
  renderCountText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
  new Label(this, SWT.NONE).setText("Clipping Mask Count:")
  private val clippingCountText = new Text(this, SWT.READ_ONLY | SWT.BORDER)
  clippingCountText.setText("0")
  clippingCountText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
  
  startTestButton.addListener(SWT.Selection, _ => startPreDrawPostDrawTest())
  stressTestButton.addListener(SWT.Selection, _ => startStressTest())
  
  def setDemoApp(app: DemoApp): Unit = {
    demoApp = Some(app)
    startTestButton.setEnabled(true)
    stressTestButton.setEnabled(true)
  }
  
  private def startPreDrawPostDrawTest(): Unit = {
    demoApp.foreach { app =>
      statusText.setText("Running...")
      // Add specific test logic here
      // For example, continuously switch expressions and actions to test preDraw/postDraw flow
    }
  }
  
  private def startStressTest(): Unit = {
    demoApp.foreach { app =>
      statusText.setText("Stress Test Running...")
          // Implement 10-minute continuous rendering test
    // Verify that preDraw/postDraw flow remains correct after long-term operation
    }
  }
  
  def updateRenderCount(count: Int): Unit = {
    renderCountText.setText(count.toString)
  }
  
  def updateClippingCount(count: Int): Unit = {
    clippingCountText.setText(count.toString)
  }
}
