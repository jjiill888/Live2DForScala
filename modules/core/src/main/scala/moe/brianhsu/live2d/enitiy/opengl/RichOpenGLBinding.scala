package moe.brianhsu.live2d.enitiy.opengl

import moe.brianhsu.live2d.enitiy.opengl.RichOpenGLBinding.{ColorWriteMask, ViewPort}
import moe.brianhsu.live2d.enitiy.opengl.texture.TextureColor

import java.nio.ByteBuffer

object RichOpenGLBinding:
  private var wrapper: Map[OpenGLBinding, RichOpenGLBinding] = Map.empty

  def wrapOpenGLBinding(binding: OpenGLBinding): RichOpenGLBinding =
    wrapper.get(binding) match
      case Some(wrapper) => wrapper
      case None =>
        wrapper += (binding -> new RichOpenGLBinding(binding))
        wrapper(binding)

  case class ColorWriteMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean)
  case class ViewPort(x: Int, y: Int, width: Int, height: Int)

class RichOpenGLBinding(binding: OpenGLBinding):
  import binding.constants._

  private val intBuffer: scala.Array[Int] = new scala.Array[Int](10)
  private val byteBuffer: scala.Array[Byte] = new scala.Array[Byte](10)
  private val booleanBuffer: scala.Array[Boolean] = new scala.Array[Boolean](4)

  private def clearIntBuffer(): Unit =
    intBuffer.indices.foreach(intBuffer(_) = 0)

  private def clearByteBuffer(): Unit =
    byteBuffer.indices.foreach(byteBuffer(_) = 0)

  private def clearBooleanBuffer(): Unit =
    booleanBuffer.indices.foreach(booleanBuffer(_) = false)

  def openGLParameters(pname: Int): Int =
    clearIntBuffer()
    binding.glGetIntegerv(pname, intBuffer, 0)
    intBuffer(0)

  def generateTextures(count: Int): scala.List[Int] =
    require(count > 0, s"$count should >= 1")

    val newTextureBuffers: scala.Array[Int] = new scala.Array(count)
    binding.glGenTextures(count, newTextureBuffers)

    val successCount = newTextureBuffers.count(_ != 0)
    if (successCount != count) then
      throw new RuntimeException(s"Cannot generate all textures, expected count: $count, actual count: $successCount")

    newTextureBuffers.toList

  def generateFrameBuffers(count: Int): scala.List[Int] =
    require(count > 0, s"$count should >= 1")

    val newFrameBuffers: scala.Array[Int] = new scala.Array(count)
    binding.glGenFramebuffers(count, newFrameBuffers)

    val successCount = newFrameBuffers.count(_ != 0)
    if (successCount != count) then
      throw new RuntimeException(s"Cannot generate all buffers, expected count: $count, actual count: $successCount")

    newFrameBuffers.toList

  def textureBinding2D(textureUnit: Int): Int =
    binding.glActiveTexture(textureUnit)
    openGLParameters(GL_TEXTURE_BINDING_2D)

  def activeAndBinding2DTexture(textureUnit: Int, textureName: Int): Unit =
    binding.glActiveTexture(textureUnit)
    binding.glBindTexture(GL_TEXTURE_2D, textureName)

  def blendFunction: BlendFunction =
    clearIntBuffer()

    binding.glGetIntegerv(GL_BLEND_SRC_RGB, intBuffer, 0)
    binding.glGetIntegerv(GL_BLEND_DST_RGB, intBuffer, 1)
    binding.glGetIntegerv(GL_BLEND_SRC_ALPHA, intBuffer, 2)
    binding.glGetIntegerv(GL_BLEND_DST_ALPHA, intBuffer, 3)

    BlendFunction(intBuffer(0), intBuffer(1), intBuffer(2), intBuffer(3))

  def blendFunction_=(blendFunction: BlendFunction): Unit =
    binding.glBlendFuncSeparate(blendFunction.sourceRGB, blendFunction.destRGB, blendFunction.sourceAlpha, blendFunction.destAlpha)

  def colorWriteMask: ColorWriteMask =
    clearByteBuffer()

    binding.glGetBooleanv(GL_COLOR_WRITEMASK, byteBuffer)

    ColorWriteMask(
      byteBuffer(0) != 0, byteBuffer(1) != 0,
      byteBuffer(2) != 0, byteBuffer(3) != 0
    )

  def colorWriteMask_=(mask: ColorWriteMask): Unit =
    binding.glColorMask(mask.red, mask.green, mask.blue, mask.alpha)

  def viewPort: ViewPort =
    clearIntBuffer()

    binding.glGetIntegerv(GL_VIEWPORT, intBuffer)

    ViewPort(intBuffer(0), intBuffer(1), intBuffer(2), intBuffer(3))

  def viewPort_=(viewPort: ViewPort): Unit =
    binding.glViewport(viewPort.x, viewPort.y, viewPort.width, viewPort.height)

  def vertexAttributes: scala.Array[Boolean] =
    clearBooleanBuffer()

    binding.glGetVertexAttribiv(0, GL_VERTEX_ATTRIB_ARRAY_ENABLED, intBuffer, 0)
    binding.glGetVertexAttribiv(1, GL_VERTEX_ATTRIB_ARRAY_ENABLED, intBuffer, 1)
    binding.glGetVertexAttribiv(2, GL_VERTEX_ATTRIB_ARRAY_ENABLED, intBuffer, 2)
    binding.glGetVertexAttribiv(3, GL_VERTEX_ATTRIB_ARRAY_ENABLED, intBuffer, 3)

    booleanBuffer(0) = intBuffer(0) != 0
    booleanBuffer(1) = intBuffer(1) != 0
    booleanBuffer(2) = intBuffer(2) != 0
    booleanBuffer(3) = intBuffer(3) != 0

    booleanBuffer

  def vertexAttributes_=(buffer: scala.Array[Boolean]): Unit =
    binding.glEnableVertexAttribArray(0)
    binding.glEnableVertexAttribArray(1)
    binding.glEnableVertexAttribArray(2)
    binding.glEnableVertexAttribArray(3)

  def preDraw(): Unit = {
    // Disable unnecessary tests
    binding.glDisable(GL_SCISSOR_TEST)
    binding.glDisable(GL_STENCIL_TEST)
    binding.glDisable(GL_DEPTH_TEST)
    
    // Enable blending
    binding.glEnable(GL_BLEND)
    binding.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
    
    // Set color mask
    binding.glColorMask(true, true, true, true)
    
    // Unbind buffers
    binding.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    binding.glBindBuffer(GL_ARRAY_BUFFER, 0)
  }

  def postDraw(): Unit = {
    // Restore default state
    binding.glEnable(GL_DEPTH_TEST)
    binding.glDisable(GL_BLEND)
    
    // Clean up texture bindings
    binding.glActiveTexture(GL_TEXTURE0)
    binding.glBindTexture(GL_TEXTURE_2D, 0)
    binding.glActiveTexture(GL_TEXTURE1)
    binding.glBindTexture(GL_TEXTURE_2D, 0)
    
    // Clean up program
    binding.glUseProgram(0)
  }

  def setCapabilityEnabled(capability: Int, enabled: Boolean): Unit =
    if enabled then binding.glEnable(capability) else binding.glDisable(capability)

  def updateVertexInfo(vertexArray: java.nio.ByteBuffer, uvArray: java.nio.ByteBuffer, positionLocation: Int, uvLocation: Int): Unit =
    binding.glVertexAttribPointer(positionLocation, 2, GL_FLOAT, false, 0, vertexArray)
    binding.glVertexAttribPointer(uvLocation, 2, GL_FLOAT, false, 0, uvArray)

  def activeAndUpdateTextureVariable(textureUnit: Int, textureId: Int, uniformLocation: Int, textureUnitIndex: Int): Unit =
    binding.glActiveTexture(textureUnit)
    binding.glBindTexture(GL_TEXTURE_2D, textureId)
    binding.glUniform1i(uniformLocation, textureUnitIndex)

  def setColorChannel(colorChannel: TextureColor, uniformLocation: Int): Unit =
    binding.glUniform4f(uniformLocation, colorChannel.red, colorChannel.green, colorChannel.blue, colorChannel.alpha)