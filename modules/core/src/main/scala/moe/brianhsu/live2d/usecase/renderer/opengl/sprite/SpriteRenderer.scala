package moe.brianhsu.live2d.usecase.renderer.opengl.sprite

import moe.brianhsu.live2d.enitiy.opengl.OpenGLBinding
import moe.brianhsu.live2d.enitiy.opengl.sprite.Sprite
import moe.brianhsu.live2d.usecase.renderer.opengl.shader.SpriteShader

class SpriteRenderer(spriteShader: SpriteShader)(implicit gl: OpenGLBinding) {

  import gl.constants._

  private val positionVertex = new Array[Float](8)
  private val uvVertex = Array(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)

  private val positionBuffer = gl.newDirectFloatBuffer(positionVertex)
  private val uvBuffer = gl.newDirectFloatBuffer(uvVertex)
  uvBuffer.put(uvVertex).flip()

  def draw(sprite: Sprite): Unit = {
    val maxWidth = sprite.drawCanvasInfoReader.currentCanvasWidth
    val maxHeight = sprite.drawCanvasInfoReader.currentCanvasHeight
    val halfWidth = maxWidth * 0.5f
    val halfHeight = maxHeight * 0.5f

    positionVertex(0) = (sprite.positionAndSize.rightX - halfWidth) / halfWidth
    positionVertex(1) = (sprite.positionAndSize.topY - halfHeight) / halfHeight
    positionVertex(2) = (sprite.positionAndSize.leftX - halfWidth) / halfWidth
    positionVertex(3) = (sprite.positionAndSize.topY - halfHeight) / halfHeight
    positionVertex(4) = (sprite.positionAndSize.leftX - halfWidth) / halfWidth
    positionVertex(5) = (sprite.positionAndSize.bottomY - halfHeight) / halfHeight
    positionVertex(6) = (sprite.positionAndSize.rightX - halfWidth) / halfWidth
    positionVertex(7) = (sprite.positionAndSize.bottomY - halfHeight) / halfHeight

    positionBuffer.clear()
    positionBuffer.put(positionVertex).flip()

    gl.glUseProgram(spriteShader.programId)
    gl.glEnable(GL_TEXTURE_2D)

    gl.glEnableVertexAttribArray(spriteShader.positionLocation)
    gl.glEnableVertexAttribArray(spriteShader.uvLocation)
    gl.glUniform1i(spriteShader.textureLocation, 0)

    gl.glVertexAttribPointer(spriteShader.positionLocation, 2, GL_FLOAT, normalized = false, 0, positionBuffer)
    gl.glVertexAttribPointer(spriteShader.uvLocation, 2, GL_FLOAT, normalized = false, 0, uvBuffer)

    gl.glUniform4f(spriteShader.baseColorLocation, sprite.spriteColor.red, sprite.spriteColor.green, sprite.spriteColor.blue, sprite.spriteColor.alpha)
    gl.glBindTexture(GL_TEXTURE_2D, sprite.textureInfo.textureId)
    gl.glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
  }
}
