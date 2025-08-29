package moe.brianhsu.live2d.demo.javafx.widget

import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.{GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import javafx.embed.swing.SwingNode
import javafx.scene.layout.StackPane
import javafx.scene.input.{KeyEvent => JFXKeyEvent}
import moe.brianhsu.live2d.adapter.gateway.opengl.jogl.JavaOpenGLBinding
import moe.brianhsu.live2d.adapter.gateway.renderer.jogl.JOGLCanvasInfoReader
import moe.brianhsu.live2d.demo.app.DemoApp

import java.awt.event.{KeyEvent, KeyListener, MouseAdapter, MouseEvent, MouseMotionAdapter, MouseWheelEvent, MouseWheelListener}
import java.awt.BorderLayout
import javax.swing.{JPanel, SwingUtilities}

object JavaFXAvatarDisplayArea {
  trait AvatarListener {
    def onAvatarLoaded(live2DView: DemoApp): Unit
    def onStatusUpdated(status: String): Unit
  }
}

class JavaFXAvatarDisplayArea extends StackPane {
  import JavaFXAvatarDisplayArea._

  private val profile = GLProfile.get(GLProfile.GL2)
  private val capabilities = new GLCapabilities(profile)
  capabilities.setAlphaBits(8)
  capabilities.setBackgroundOpaque(false)
  private val glCanvas = new GLCanvas(capabilities)
  private val swingNode = new SwingNode()

  private val canvasInfo = new JOGLCanvasInfoReader(glCanvas)
  private var demoAppHolder: Option[DemoApp] = None
  private var animator: Option[FixedFPSAnimator] = None
  private var avatarListenerHolder: Option[AvatarListener] = None
  private var lastMouseX: Option[Int] = None
  private var lastMouseY: Option[Int] = None
  private var demoAppReadyListener: Option[DemoApp => Unit] = None

  createSwingContent()
  setupEventHandlers()
  glCanvas.addGLEventListener(new CanvasGLEventListener)

  def demoApp: Option[DemoApp] = demoAppHolder
  def setAvatarListener(listener: AvatarListener): Unit = { avatarListenerHolder = Some(listener) }
  def onDemoAppReady(listener: DemoApp => Unit): Unit = { demoAppReadyListener = Some(listener) }

  private def createSwingContent(): Unit = {
    val panel = new JPanel(new BorderLayout())
    panel.add(glCanvas)
    SwingUtilities.invokeLater(() => swingNode.setContent(panel))
    this.getChildren.add(swingNode)
  }

  private def setupEventHandlers(): Unit = {
    glCanvas.addMouseMotionListener(new MouseMotionAdapter() {
      override def mouseMoved(e: MouseEvent): Unit = demoAppHolder.foreach(_.onMouseMoved(e.getX, e.getY))
      override def mouseDragged(e: MouseEvent): Unit = {
        if (SwingUtilities.isLeftMouseButton(e)) {
          demoAppHolder.foreach(_.onMouseDragged(e.getX, e.getY))
        }
        if (SwingUtilities.isRightMouseButton(e)) {
          val offsetX = lastMouseX.map(e.getX - _).getOrElse(0).toFloat * 0.002f
          val offsetY = lastMouseY.map(_ - e.getY).getOrElse(0).toFloat * 0.002f
          demoAppHolder.foreach(_.move(offsetX, offsetY))
          lastMouseX = Some(e.getX)
          lastMouseY = Some(e.getY)
        }
      }
    })

    glCanvas.addMouseListener(new MouseAdapter() {
      override def mouseReleased(e: MouseEvent): Unit = {
        demoAppHolder.foreach(_.onMouseReleased(e.getX, e.getY))
        lastMouseX = None
        lastMouseY = None
      }
    })

    glCanvas.addMouseWheelListener(new MouseWheelListener {
      override def mouseWheelMoved(e: MouseWheelEvent): Unit = {
        demoAppHolder.foreach(_.zoom(e.getScrollAmount * -e.getWheelRotation * 0.01f))
      }
    })

    glCanvas.addKeyListener(new KeyListener {
      override def keyTyped(e: KeyEvent): Unit = {}
      override def keyPressed(e: KeyEvent): Unit = {}
      override def keyReleased(e: KeyEvent): Unit = demoAppHolder.foreach(_.keyReleased(e.getKeyChar))
    })

    this.addEventHandler(JFXKeyEvent.ANY, (_: JFXKeyEvent) => glCanvas.requestFocusInWindow())
  }

  private def runOnOpenGLThread(callback: => Any): Any = {
    glCanvas.invoke(true, (_: GLAutoDrawable) => {
      callback
      true
    })
  }

  private class CanvasGLEventListener extends GLEventListener {
    override def init(drawable: GLAutoDrawable): Unit = {
      given openGL: JavaOpenGLBinding = new JavaOpenGLBinding(drawable.getGL.getGL2)
      val app = new DemoApp(canvasInfo, runOnOpenGLThread) {
        override def onAvatarLoaded(live2DView: DemoApp): Unit =
          avatarListenerHolder.foreach(_.onAvatarLoaded(live2DView))
        override def onStatusUpdated(status: String): Unit =
          avatarListenerHolder.foreach(_.onStatusUpdated(status))
      }
      app.setTransparentBackground(DemoApp.loadTransparentBackground())
      demoAppHolder = Some(app)
      demoAppReadyListener.foreach(_(demoAppHolder.get))
      animator = Some(new FixedFPSAnimator(60, drawable))
      animator.foreach(_.start())
    }

    override def dispose(drawable: GLAutoDrawable): Unit = {
      animator.foreach(_.stop())
    }

    override def display(drawable: GLAutoDrawable): Unit = {
      demoAppHolder.foreach(_.display())
            // Swap buffers manually if auto swap is disabled
      if (!glCanvas.getAutoSwapBufferMode) {
        glCanvas.swapBuffers()
      }
    }

    override def reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit = {
      demoAppHolder.foreach(_.resize())
    }
  }
}