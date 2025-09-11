package moe.brianhsu.live2d.adapter.gateway.opengl.lwjgl

import javafx.scene.canvas.Canvas
import moe.brianhsu.live2d.boundary.gateway.renderer.DrawCanvasInfoReader

class JavaFXOpenGLCanvasInfoReader(canvas: Canvas) extends DrawCanvasInfoReader {
  override def currentCanvasWidth: Int = canvas.getWidth.toInt
  override def currentCanvasHeight: Int = canvas.getHeight.toInt
  override def currentSurfaceWidth: Int = canvas.getWidth.toInt
  override def currentSurfaceHeight: Int = canvas.getHeight.toInt
}