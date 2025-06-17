package moe.brianhsu.live2d.demo.javafx.widget

import com.jogamp.opengl.GLAutoDrawable
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

/** Simple fixed FPS animator used to refresh the JOGL canvas. */
class FixedFPSAnimator(fps: Int, drawable: GLAutoDrawable) {
  private val scheduledThreadPool = new ScheduledThreadPoolExecutor(1)
  private val updateOpenGLCanvas = new Runnable {
    override def run(): Unit = {
      try {
        drawable.display()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def start(): Unit = {
    createScheduledFuture()
  }

  def stop(): Unit = {
    scheduledThreadPool.shutdown()
  }

  private def createScheduledFuture(): Unit = {
    scheduledThreadPool.scheduleAtFixedRate(
      updateOpenGLCanvas, 0, calculateExecutionPeriod,
      TimeUnit.MILLISECONDS
    )
  }

  private def calculateExecutionPeriod: Int = {
    val updateIntervalInSeconds = 1 / fps.toDouble
    val updateIntervalInMilliSeconds = updateIntervalInSeconds * 1000
    updateIntervalInMilliSeconds.toInt
  }
}