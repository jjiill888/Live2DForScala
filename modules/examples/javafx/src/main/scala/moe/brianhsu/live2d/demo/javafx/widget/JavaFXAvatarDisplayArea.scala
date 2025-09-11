package moe.brianhsu.live2d.demo.javafx.widget

import javafx.animation.AnimationTimer
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.input.{KeyEvent, MouseEvent, ScrollEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import moe.brianhsu.live2d.adapter.gateway.opengl.lwjgl.{JavaFXOpenGLCanvasInfoReader, LWJGLBinding}
import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.javafx.widget.JavaFXAvatarDisplayArea.AvatarListener
import scala.util.Try

object JavaFXAvatarDisplayArea {
  trait AvatarListener {
    def onAvatarLoaded(live2DView: DemoApp): Unit
    def onStatusUpdated(status: String): Unit
  }
}

class JavaFXAvatarDisplayArea extends Pane {
  private var lastX: Option[Double] = None
  private var lastY: Option[Double] = None
  private var avatarListenerHolder: Option[AvatarListener] = None
  private var initialized = false

  private val canvas = new Canvas(800, 600)
  private given openGLBinding: LWJGLBinding = new LWJGLBinding
  private val canvasInfo = new JavaFXOpenGLCanvasInfoReader(canvas)

  // Delay DemoApp creation until OpenGL is ready
  private var demoAppHolder: Option[DemoApp] = None
  
  def demoApp: DemoApp = {
    demoAppHolder.getOrElse {
      throw new RuntimeException("DemoApp not initialized yet. Call initializeOpenGL first.")
    }
  }

  private val animationTimer = new AnimationTimer {
    private val FrameRate: Int = {
      val overrideValue =
        sys.props.get("live2d.frameRate").orElse(sys.env.get("LIVE2D_FRAME_RATE"))
          .flatMap(v => Try(v.toInt).toOption)

      overrideValue.getOrElse {
        System.getProperty("os.name").toLowerCase match {
          case os if os.contains("windows") => 144
          case os if os.contains("linux")   => 60
          case _ => 60
        }
      }
    }
    
    private val frameTimeNanos = (1_000_000_000.0 / FrameRate).toLong
    private var lastTime: Long = 0
    
    override def handle(now: Long): Unit = {
      if (now - lastTime >= frameTimeNanos) {
        if (initialized) {
          // For now, just clear the canvas - we'll add Live2D rendering later
          val gc = canvas.getGraphicsContext2D
          gc.clearRect(0, 0, canvas.getWidth, canvas.getHeight)
          gc.setFill(Color.BLACK)
          gc.fillRect(0, 0, canvas.getWidth, canvas.getHeight)
          
          // Add some basic rendering indication
          gc.setFill(Color.WHITE)
          gc.fillText("Live2D JavaFX Demo - Basic Canvas", 10, 30)
          gc.fillText(s"Canvas Size: ${canvas.getWidth.toInt} x ${canvas.getHeight.toInt}", 10, 50)
        }
        lastTime = now
      }
    }
  }

  // Initialize everything
  setupEventHandlers()
  getChildren.add(canvas)

  def setAvatarListener(listener: AvatarListener): Unit = {
    this.avatarListenerHolder = Some(listener)
  }

  def initializeOpenGL(): Unit = {
    if (initialized) return
    
    println("Initializing JavaFX Canvas...")
    initialized = true
    
    // For now, skip DemoApp creation until we can properly initialize OpenGL context
    println("Skipping DemoApp creation - OpenGL context needs to be properly initialized")
    
    animationTimer.start()
    avatarListenerHolder.foreach(_.onStatusUpdated("JavaFX Canvas initialized (basic mode - Live2D integration pending)"))
    println("JavaFX Canvas running in basic mode")
  }

  private def setupEventHandlers(): Unit = {
    // Mouse events
    canvas.setOnMouseMoved(this.handleMouseMove)
    canvas.setOnMouseDragged(this.handleMouseDrag)
    canvas.setOnMouseReleased(this.handleMouseRelease)
    canvas.setOnScroll(this.handleScroll)
    
    // Key events
    canvas.setOnKeyReleased(this.handleKeyRelease)
    
    // Make canvas focusable for key events
    canvas.setFocusTraversable(true)
    
    // Resize handlers
    canvas.widthProperty().addListener((_, _, _) => {
      if (initialized) {
        println(s"Canvas resized to: ${canvas.getWidth} x ${canvas.getHeight}")
      }
    })
    
    canvas.heightProperty().addListener((_, _, _) => {
      if (initialized) {
        println(s"Canvas resized to: ${canvas.getWidth} x ${canvas.getHeight}")
      }
    })

    // Bind canvas size to pane size
    canvas.widthProperty().bind(this.widthProperty())
    canvas.heightProperty().bind(this.heightProperty())
  }

  private def handleMouseMove(event: MouseEvent): Unit = {
    // For now, just print mouse coordinates
    // demoApp.onMouseMoved(event.getX.toInt, event.getY.toInt)
  }

  private def handleMouseDrag(event: MouseEvent): Unit = {
    if (event.isPrimaryButtonDown) {
      println(s"Mouse dragged at: ${event.getX}, ${event.getY}")
    } else if (event.isSecondaryButtonDown) {
      val offsetX = lastX.map(event.getX - _).getOrElse(0.0).toFloat * 0.002f
      val offsetY = lastY.map(_ - event.getY).getOrElse(0.0).toFloat * 0.002f
      
      println(s"Right drag offset: $offsetX, $offsetY")
      
      lastX = Some(event.getX)
      lastY = Some(event.getY)
    }
  }

  private def handleMouseRelease(event: MouseEvent): Unit = {
    if (event.getButton.toString == "PRIMARY") {
      println(s"Mouse released at: ${event.getX}, ${event.getY}")
    }
    lastX = None
    lastY = None
  }

  private def handleScroll(event: ScrollEvent): Unit = {
    println(s"Mouse scroll: ${event.getDeltaY}")
  }

  private def handleKeyRelease(event: KeyEvent): Unit = {
    if (event.getText.nonEmpty) {
      println(s"Key released: ${event.getText.charAt(0)}")
    }
  }

  private def runOnOpenGLThread(callback: => Any): Any = {
    // For JavaFX, we'll execute on the JavaFX Application Thread
    if (javafx.application.Platform.isFxApplicationThread()) {
      callback
    } else {
      var result: Any = null
      val latch = new java.util.concurrent.CountDownLatch(1)
      javafx.application.Platform.runLater(() => {
        try {
          result = callback
        } finally {
          latch.countDown()
        }
      })
      latch.await()
      result
    }
  }

  def cleanup(): Unit = {
    animationTimer.stop()
    println("JavaFX Canvas cleanup completed")
  }
}