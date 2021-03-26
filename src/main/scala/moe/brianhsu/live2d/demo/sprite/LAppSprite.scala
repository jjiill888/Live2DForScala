package moe.brianhsu.live2d.demo.sprite

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.GLAutoDrawable
import moe.brianhsu.live2d.adapter.JavaOpenGL
import moe.brianhsu.live2d.framework.math.Rectangle
import moe.brianhsu.live2d.renderer.opengl.TextureManager.TextureInfo

abstract class LAppSprite(drawable: GLAutoDrawable, textureInfo: TextureInfo, shader: SpriteShader) {

  case class Position(originX: Float, originY: Float, width: Float, height: Float)

  private val gl = new JavaOpenGL(drawable.getGL.getGL2)

  import gl._

  private val positionLocation = gl.glGetAttribLocation(shader.shaderProgram, "position")
  private val uvLocation = gl.glGetAttribLocation(shader.shaderProgram, "uv")
  private val textureLocation = gl.glGetUniformLocation(shader.shaderProgram, "texture")
  private val colorLocation = gl.glGetUniformLocation(shader.shaderProgram, "baseColor")
  private val spriteColor = (1.0f, 1.0f, 1.0f, 1.0f)
  private var rect = createRectangle()

  protected def calculatePosition(): Position

  private def createRectangle() = {
    val position = calculatePosition()

    Rectangle(
      position.originX - position.width * 0.5f,
      position.originY - position.height * 0.5f,
      position.width, position.height
    )
  }

  def resize(): Unit = {
    this.rect = createRectangle()
  }

  def render(): Unit = {
    val maxWidth = drawable.getSurfaceWidth
    val maxHeight = drawable.getSurfaceHeight

    gl.glEnable(GL_TEXTURE_2D)

    val uvVertex = Array(
        1.0f, 0.0f,
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    )

    gl.glEnableVertexAttribArray(positionLocation)
    gl.glEnableVertexAttribArray(uvLocation)
    gl.glUniform1i(textureLocation, 0)

    val positionVertex = Array(
      (rect.rightX - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.bottomY - maxHeight * 0.5f) / (maxHeight * 0.5f),
      (rect.leftX - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.bottomY - maxHeight * 0.5f) / (maxHeight * 0.5f),
      (rect.leftX - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.topY - maxHeight * 0.5f) / (maxHeight * 0.5f),
      (rect.rightX - maxWidth * 0.5f) / (maxWidth * 0.5f), (rect.topY - maxHeight * 0.5f) / (maxHeight * 0.5f)
    )

    val buffer1 = Buffers.newDirectFloatBuffer(positionVertex)
    val buffer2 = Buffers.newDirectFloatBuffer(uvVertex)

    gl.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, normalized = false, 0, buffer1)
    gl.glVertexAttribPointer(uvLocation, 2, GL_FLOAT, normalized = false, 0, buffer2)

    gl.glUniform4f(colorLocation, spriteColor._1, spriteColor._2, spriteColor._3, spriteColor._4)
    gl.glBindTexture(GL_TEXTURE_2D, textureInfo.textureId)
    gl.glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
  }

}