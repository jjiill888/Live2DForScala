package moe.brianhsu.porting.live2d.renderer.opengl.shader

import moe.brianhsu.live2d.enitiy.model.drawable.ConstantFlags.{AdditiveBlend, BlendMode, MultiplicativeBlend, Normal}
import moe.brianhsu.live2d.enitiy.opengl.OpenGLBinding
import moe.brianhsu.live2d.usecase.renderer.opengl.shader.{AvatarShader, InvertedMaskedShader, MaskedShader, NormalShader, SetupMaskShader}
import moe.brianhsu.live2d.usecase.renderer.opengl.texture.TextureColor
import moe.brianhsu.live2d.usecase.renderer.viewport.matrix.ProjectionMatrix
import moe.brianhsu.porting.live2d.renderer.opengl.Renderer
import moe.brianhsu.porting.live2d.renderer.opengl.clipping.ClippingContext

import java.nio.ByteBuffer

object ShaderRenderer {
  private var shaderRendererHolder: Map[OpenGLBinding, ShaderRenderer] = Map.empty

  def getInstance(implicit gl: OpenGLBinding): ShaderRenderer = {
    shaderRendererHolder.get(gl) match {
      case Some(renderer) => renderer
      case None =>
        this.shaderRendererHolder += (gl -> new ShaderRenderer())
        this.shaderRendererHolder(gl)
    }
  }
}

class ShaderRenderer private (implicit gl: OpenGLBinding) {

  import gl.constants._

  private val setupMaskShader = new SetupMaskShader
  private val normalShader = new NormalShader
  private val maskedShader = new MaskedShader
  private val invertedMaskedShader = new InvertedMaskedShader

  case class Blending(srcColor: Int, dstColor: Int, srcAlpha: Int, dstAlpha: Int)

  def render(renderer: Renderer, textureId: Int,
             vertexArray: ByteBuffer, uvArray: ByteBuffer, colorBlendMode: BlendMode,
             baseColor: TextureColor, projection: ProjectionMatrix,
             invertedMask: Boolean): Unit = {

    renderer.getClippingContextBufferForMask match {
      case Some(context) => renderMask(context, textureId, vertexArray, uvArray)
      case None => renderDrawable(renderer, textureId, vertexArray, uvArray, colorBlendMode, baseColor, projection, invertedMask)
    }
  }

  private def renderDrawable(renderer: Renderer, textureId: Int, vertexArray: ByteBuffer, uvArray: ByteBuffer, colorBlendMode: BlendMode, baseColor: TextureColor, projection: ProjectionMatrix, invertedMask: Boolean): Unit = {
    val drawClippingContextHolder = renderer.getClippingContextBufferForDraw
    val masked = drawClippingContextHolder.isDefined // この描画オブジェクトはマスク対象か
    val shader = masked match {
      case true if invertedMask => invertedMaskedShader
      case true => maskedShader
      case false => normalShader
    }

    val blending = colorBlendMode match {
      case Normal => Blending(GL_ONE, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
      case AdditiveBlend => Blending(GL_ONE, GL_ONE, GL_ZERO, GL_ONE)
      case MultiplicativeBlend => Blending(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE)
    }

    shader.useProgram()

    setGlVertexInfo(vertexArray, uvArray, shader)

    for (context <- drawClippingContextHolder) {
      for {
        buffer <- renderer.offscreenBufferHolder
        textureId <- buffer.bufferIds.textureBufferHolder
      } {
        setGlTexture(GL_TEXTURE1, textureId, shader.samplerTexture1Location, 1)
        gl.glUniformMatrix4fv(shader.uniformClipMatrixLocation, 1, transpose = false, context.getMatrixForDraw.elements)
        setGlColorChannel(context, shader)
      }
    }

    //テクスチャ設定
    setGlTexture(GL_TEXTURE0, textureId, shader.samplerTexture0Location, 0)

    //座標変換
    gl.glUniformMatrix4fv(shader.uniformMatrixLocation, 1, transpose = false, projection.elements)
    gl.glUniform4f(shader.uniformBaseColorLocation, baseColor.red, baseColor.green, baseColor.blue, baseColor.alpha)
    setGlBlend(blending)
  }

  private def renderMask(context: ClippingContext, textureId: Int, vertexArray: ByteBuffer, uvArray: ByteBuffer): Unit = {
    val shader = setupMaskShader

    shader.useProgram()

    setGlTexture(GL_TEXTURE0, textureId, shader.samplerTexture0Location, 0)
    setGlVertexInfo(vertexArray, uvArray, shader)
    setGlColorChannel(context, shader)

    gl.glUniformMatrix4fv(shader.uniformClipMatrixLocation, 1, transpose = false, context.getMatrixForMask.elements)

    val rect = context.getLayoutBounds

    gl.glUniform4f(
      shader.uniformBaseColorLocation,
      rect.leftX * 2.0f - 1.0f,
      rect.bottomY * 2.0f - 1.0f,
      rect.rightX * 2.0f - 1.0f,
      rect.topY * 2.0f - 1.0f
    )

    setGlBlend(Blending(GL_ZERO, GL_ONE_MINUS_SRC_COLOR, GL_ZERO, GL_ONE_MINUS_SRC_ALPHA))
  }

  def setGlColorChannel(context: ClippingContext, shader: AvatarShader): Unit = {
    val colorChannel = context.getChannelColor
    gl.glUniform4f(shader.uniformChannelFlagLocation, colorChannel.red, colorChannel.green, colorChannel.blue, colorChannel.alpha)

  }

  def setGlTexture(textureUnit: Int, textureId: Int, variable: Int, variableValue: Int): Unit = {
    gl.glActiveTexture(textureUnit)
    gl.glBindTexture(GL_TEXTURE_2D, textureId)
    gl.glUniform1i(variable, variableValue)
  }

  private def setGlVertexInfo(vertexArray: ByteBuffer, uvArray: ByteBuffer, shader: AvatarShader): Unit = {
    // 頂点配列の設定
    gl.glEnableVertexAttribArray(shader.attributePositionLocation)
    gl.glVertexAttribPointer(shader.attributePositionLocation, 2, GL_FLOAT, normalized = false, 4 * 2, vertexArray)
    // テクスチャ頂点の設定
    gl.glEnableVertexAttribArray(shader.attributeTexCoordLocation)
    gl.glVertexAttribPointer(shader.attributeTexCoordLocation, 2, GL_FLOAT, normalized = false, 4 * 2, uvArray)
  }

  private def setGlBlend(blending: Blending): Unit = {
    gl.glBlendFuncSeparate(blending.srcColor, blending.dstColor, blending.srcAlpha, blending.dstAlpha)
  }

}