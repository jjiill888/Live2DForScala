package moe.brianhsu.live2d.demo.swt

import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.swt.widget.SWTAvatarDisplayArea.AvatarListener
import moe.brianhsu.live2d.demo.swt.widget.{SWTAvatarControlPanel, SWTAvatarDisplayArea, SWTStatusBar, SWTToolbar}
import moe.brianhsu.live2d.adapter.util.WaylandSupport
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.layout.{GridData, GridLayout}
import org.eclipse.swt.widgets.{Display, Shell, Listener, Event}

object SWTWithLWJGLMain {

  WaylandSupport.setup()
  
  private val display = new Display()
  private val shell = new Shell(display)
  private val toolbar = new SWTToolbar(shell)
  private val sashForm = new SashForm(shell, SWT.HORIZONTAL|SWT.SMOOTH)
  private val avatarControl = new SWTAvatarControlPanel(sashForm)
  private val avatarArea = new SWTAvatarDisplayArea(sashForm)
  private val statusBar = new SWTStatusBar(shell)

  private var hideTask: Runnable = _
  private val uiListener: Listener = (_: Event) => showUIForTimeout()

  def main(args: Array[String]): Unit = {
    setupUILayout()
    setupAvatarEventListener()

    shell.setText("Live 2D Scala Demo (SWT+LWJGL)")
    
    // Load window settings on startup
    loadWindowSettings()
    
    shell.open()
    
    // Add window listener to save settings on close
    shell.addListener(SWT.Close, new Listener {
      override def handleEvent(event: Event): Unit = {
        saveWindowSettings()
      }
    })
    
    display.asyncExec(() => loadInitialAvatar())

    while (!shell.isDisposed) {
      if (!display.readAndDispatch()) {
        display.sleep()
      }
    }
    display.dispose()
    System.exit(0)

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
      }
      override def onStatusUpdated(status: String): Unit = statusBar.updateStatus(status)
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
    loadResult.failed.foreach { e =>
      System.err.println(s"[WARN] Cannot load default avatar: ${e.getMessage}")
    }
  }

  def enterCaptureMode(): Unit = {
    hideUI()
    avatarArea.glCanvas.addListener(SWT.MouseMove, uiListener)
    avatarArea.glCanvas.addListener(SWT.MouseDown, uiListener)
  }

  def exitCaptureMode(): Unit = {
    avatarArea.glCanvas.removeListener(SWT.MouseMove, uiListener)
    avatarArea.glCanvas.removeListener(SWT.MouseDown, uiListener)
    cancelHideTask()
    showUI()
  }

  private def showUIForTimeout(): Unit = {
    showUI()
    cancelHideTask()
    hideTask = () => hideUI()
    display.timerExec(2000, hideTask)
  }

  private def cancelHideTask(): Unit = {
    if (hideTask != null) display.timerExec(-1, hideTask)
  }

  private def hideUI(): Unit = {
    toolbar.setVisible(false)
    statusBar.setVisible(false)
    avatarControl.setVisible(false)
    toolbar.getLayoutData.asInstanceOf[GridData].exclude = true
    statusBar.getLayoutData.asInstanceOf[GridData].exclude = true
    sashForm.setSashWidth(0)
    sashForm.setWeights(0, 1)
    shell.layout()
  }

  private def showUI(): Unit = {
    toolbar.setVisible(true)
    statusBar.setVisible(true)
    avatarControl.setVisible(true)
    toolbar.getLayoutData.asInstanceOf[GridData].exclude = false
    statusBar.getLayoutData.asInstanceOf[GridData].exclude = false
    sashForm.setSashWidth(5)
    sashForm.setWeights(1, 4)
    shell.layout()
  }

  // Load window settings from saved configuration
  private def loadWindowSettings(): Unit = {
    moe.brianhsu.live2d.demo.app.DemoApp.loadWindowSettings() match {
      case Some((x, y, width, height, maximized)) =>
        shell.setBounds(x, y, width, height)
        if (maximized) {
          shell.setMaximized(true)
        }
      case None =>
        // Use default size if no settings found
        shell.setSize(1080, 720)
        shell.setLocation(100, 100)
    }
  }

  // Save current window settings
  private def saveWindowSettings(): Unit = {
    val bounds = shell.getBounds()
    val maximized = shell.getMaximized()
    moe.brianhsu.live2d.demo.app.DemoApp.saveWindowSettings(bounds.x, bounds.y, bounds.width, bounds.height, maximized)
  }

}
