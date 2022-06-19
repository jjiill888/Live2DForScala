package moe.brianhsu.live2d.enitiy.opengl

import moe.brianhsu.live2d.enitiy.opengl.RichOpenGLBinding.{BlendFunction, ColorWriteMask, ViewPort}

import reflect.runtime.universe._

object RichOpenGLBinding {
  private var wrapper: Map[OpenGLBinding, RichOpenGLBinding] = Map.empty

  def wrapOpenGLBinding(binding: OpenGLBinding): RichOpenGLBinding = {
    wrapper.get(binding) match {
      case Some(wrapper) => wrapper
      case None =>
        wrapper += (binding -> new RichOpenGLBinding(binding))
        wrapper(binding)
    }
  }

  case class BlendFunction(sourceRGB: Int, destRGB: Int, sourceAlpha: Int, destAlpha: Int)
  case class ColorWriteMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean)
  case class ViewPort(x: Int, y: Int, width: Int, height: Int)
}

class RichOpenGLBinding(binding: OpenGLBinding) {
  import binding.constants._

  private val intBuffer: Array[Int] = new Array[Int](10)
  private val byteBuffer: Array[Byte] = new Array[Byte](10)
  private val booleanBuffer: Array[Boolean] = new Array[Boolean](4)

  private def clearIntBuffer(): Unit = {
    intBuffer.indices.foreach(intBuffer(_) = 0)
  }

  private def clearByteBuffer(): Unit = {
    byteBuffer.indices.foreach(byteBuffer(_) = 0)
  }

  private def clearBooleanBuffer(): Unit = {
    booleanBuffer.indices.foreach(booleanBuffer(_) = false)
  }

  def openGLParameters[T: TypeTag](pname: Int): T = {
    clearIntBuffer()
    pname match {
      case _ if typeOf[T] <:< typeOf[Int] =>
        binding.glGetIntegerv(pname, intBuffer, 0)
        intBuffer(0).asInstanceOf[T]
      case _  =>
        throw new Exception("Unknown Type")
    }
  }

  def textureBinding2D(textureUnit: Int): Int = {
    binding.glActiveTexture(textureUnit)
    openGLParameters[Int](GL_TEXTURE_BINDING_2D)
  }

  def activeAndBinding2DTexture(textureUnit: Int, textureName: Int): Unit = {
    binding.glActiveTexture(textureUnit)
    binding.glBindTexture(GL_TEXTURE_2D, textureName)
  }


  def blendFunction: BlendFunction = {
    clearIntBuffer()

    binding.glGetIntegerv(GL_BLEND_SRC_RGB, intBuffer, 0)
    binding.glGetIntegerv(GL_BLEND_DST_RGB, intBuffer, 1)
    binding.glGetIntegerv(GL_BLEND_SRC_ALPHA, intBuffer, 2)
    binding.glGetIntegerv(GL_BLEND_DST_ALPHA, intBuffer, 3)

    BlendFunction(intBuffer(0), intBuffer(1), intBuffer(2), intBuffer(3))
  }

  def blendFunction_=(blendFunction: BlendFunction): Unit = {
    binding.glBlendFuncSeparate(
      blendFunction.sourceRGB,
      blendFunction.destRGB,
      blendFunction.sourceAlpha,
      blendFunction.destAlpha
    )
  }

  def viewPort: ViewPort = {
    clearIntBuffer()

    binding.glGetIntegerv(GL_VIEWPORT, intBuffer)
    ViewPort(intBuffer(0), intBuffer(1), intBuffer(2), intBuffer(3))
  }

  def viewPort_=(viewPort: ViewPort): Unit = {
    binding.glViewport(
      viewPort.x, viewPort.y,
      viewPort.width, viewPort.height
    )
  }

  def vertexAttributes: Array[Boolean] = {
    clearIntBuffer()
    clearBooleanBuffer()
    val attributeCount = 4

    for (i <- 0 until attributeCount) {
      binding.glGetVertexAttribiv(i, GL_VERTEX_ATTRIB_ARRAY_ENABLED, intBuffer, i)
      booleanBuffer(i) = intBuffer(i) == GL_TRUE
    }

    booleanBuffer
  }

  def vertexAttributes_=(buffer: Array[Boolean]): Unit = {
    for (index <- buffer.indices) {
      if (buffer(index)) {
        binding.glEnableVertexAttribArray(index)
      } else {
        binding.glDisableVertexAttribArray(index)
      }
    }
  }

  def colorWriteMask_=(colorWriteMask: ColorWriteMask): Unit = {
    binding.glColorMask(
      colorWriteMask.red, colorWriteMask.green,
      colorWriteMask.blue, colorWriteMask.alpha
    )
  }

  def colorWriteMask: ColorWriteMask = {
    clearByteBuffer()
    binding.glGetBooleanv(GL_COLOR_WRITEMASK, byteBuffer)
    ColorWriteMask(
      byteBuffer(0) == GL_TRUE, byteBuffer(1) == GL_TRUE,
      byteBuffer(2) == GL_TRUE, byteBuffer(3) == GL_TRUE
    )
  }

  def setCapabilityEnabled(capability: Int, isEnabled: Boolean): Unit = {
    if (isEnabled) {
      binding.glEnable(capability)
    } else {
      binding.glDisable(capability)
    }
  }
}