package moe.brianhsu.live2d.usecase.renderer.opengl

import moe.brianhsu.live2d.enitiy.model.Live2DModel
import moe.brianhsu.live2d.enitiy.model.drawable.ConstantFlags.BlendMode
import moe.brianhsu.live2d.enitiy.model.drawable.{DrawableColor, VertexInfo}
import moe.brianhsu.live2d.enitiy.opengl.texture.{TextureColor, TextureManager}
import moe.brianhsu.live2d.enitiy.opengl.{OpenGLBinding, RichOpenGLBinding}
import moe.brianhsu.live2d.usecase.renderer.opengl.clipping.{ClippingContext, ClippingRenderer}
import moe.brianhsu.live2d.usecase.renderer.opengl.shader.ShaderRenderer
import moe.brianhsu.live2d.usecase.renderer.viewport.matrix.ProjectionMatrix

object AvatarRenderer:

  def apply(model: Live2DModel)(using gl: OpenGLBinding): AvatarRenderer =
    val textureManager = TextureManager.getInstance
    val shaderRenderer = ShaderRenderer.getInstance
    val profile = Profile.getInstance
    val clippingRenderer = new ClippingRenderer(model, textureManager, shaderRenderer)

    new AvatarRenderer(
      model, textureManager, shaderRenderer,
      profile, clippingRenderer
    )

class AvatarRenderer(model: Live2DModel,
                     textureManager: TextureManager, shaderRenderer: ShaderRenderer, profile: Profile,
                     clippingRenderer: ClippingRenderer)
                    (using gl: OpenGLBinding):

  import gl.constants._

  def draw(projection: ProjectionMatrix): Unit = {
    this.profile.save()
    this.drawModel(model.modelMatrix * projection)
    this.profile.restore()
  }

  private def drawModel(projection: ProjectionMatrix): Unit = {
    clippingRenderer.draw(profile)

    RichOpenGLBinding.wrapOpenGLBinding(gl).preDraw()

    val sortedDrawable = model.sortedDrawables
    for (drawable <- sortedDrawable.filter(_.dynamicFlags.isVisible)) {
      val clippingContextBufferForDraw = clippingRenderer.clippingContextBufferForDraw(drawable)
      val textureFile = model.textureFiles(drawable.textureIndex)
      val textureInfo = textureManager.loadTexture(textureFile)

      drawMesh(
        clippingContextBufferForDraw,
        textureInfo.textureId,
        drawable.isCulling,
        drawable.vertexInfo,
        drawable.opacity,
        drawable.constantFlags.blendMode,
        drawable.constantFlags.isInvertedMask,
        drawable.multiplyColorFetcher(),
        drawable.screenColorFetcher(),
        projection,
      )
    }
    
            // Call postDraw to ensure OpenGL state is correctly restored
    RichOpenGLBinding.wrapOpenGLBinding(gl).postDraw()
  }

  private def drawMesh(clippingContextBufferForDraw: Option[ClippingContext],
                       drawTextureId: Int, isCulling: Boolean, vertexInfo: VertexInfo,
                       opacity: Float, colorBlendMode: BlendMode, invertedMask: Boolean,
                       multiplyColor: DrawableColor, screenColor: DrawableColor,
                       projection: ProjectionMatrix): Unit ={

    RichOpenGLBinding.wrapOpenGLBinding(gl).setCapabilityEnabled(GL_CULL_FACE, isCulling)
    gl.glFrontFace(GL_CCW)

    val modelColorRGBA = TextureColor(1.0f, 1.0f, 1.0f, opacity)

    shaderRenderer.renderDrawable(
      clippingContextBufferForDraw,
      clippingRenderer.offscreenFrameHolder,
      drawTextureId,
      vertexInfo.vertexArrayDirectBuffer, vertexInfo.uvArrayDirectBuffer,
      colorBlendMode, modelColorRGBA,
      multiplyColor, screenColor,
      projection, invertedMask
    )

    gl.glDrawElements(GL_TRIANGLES, vertexInfo.numberOfTriangleIndex, GL_UNSIGNED_SHORT, vertexInfo.indexArrayDirectBuffer)
    gl.glUseProgram(0)
  }
