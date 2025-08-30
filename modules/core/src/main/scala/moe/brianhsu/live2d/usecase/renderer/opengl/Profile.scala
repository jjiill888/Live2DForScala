package moe.brianhsu.live2d.usecase.renderer.opengl

import moe.brianhsu.live2d.enitiy.opengl.RichOpenGLBinding._
import moe.brianhsu.live2d.enitiy.opengl.{BlendFunction, OpenGLBinding, RichOpenGLBinding}

object Profile:
  private var profile: Map[OpenGLBinding, Profile] = Map.empty
  
  // Use Scala 3 given for implicit conversion
  private given Conversion[OpenGLBinding, RichOpenGLBinding] = RichOpenGLBinding.wrapOpenGLBinding

  // Factory method using given/using
  def getInstance(using gl: OpenGLBinding): Profile =
    profile.get(gl) match
      case Some(profile) => profile
      case None =>
        this.profile += (gl -> new Profile)
        this.profile.get(gl).get
  
  // Extension method for easier profile management
  extension (gl: OpenGLBinding)
    def profile: Profile = getInstance(using gl)

/**
 * The Profile state object
 *
 * This is to save / restore various OpenGL parameters.
 * This class should not be created directly by client, client should use `getInstance` method
 * in Profile object.
 */
class Profile private[opengl] (using gl: OpenGLBinding):
  import gl.constants._
  
  // State variables
  private var lastProgram: Int = 0
  private var lastVertexAttributes: scala.Array[Boolean] = new scala.Array[Boolean](4)
  private var lastScissorTest: Boolean = false
  private var lastStencilTest: Boolean = false
  private var lastDepthTest: Boolean = false
  private var lastCullFace: Boolean = false
  private var lastBlend: Boolean = false
  private var lastFrontFace: Int = 0
  private var lastColorWriteMask: ColorWriteMask = ColorWriteMask(true, true, true, true)
  private var lastArrayBufferBinding: Int = 0
  private var lastElementArrayBufferBinding: Int = 0
  private var lastTexture0Binding2D: Int = 0
  private var lastTexture1Binding2D: Int = 0
  private var lastActiveTexture: Int = 0
  private var lastBlending: BlendFunction = BlendFunction(0, 0, 0, 0)
  private var isSaved: Boolean = false

  var lastFrameBufferBinding: Int = 0
  var lastViewPort: ViewPort = ViewPort(0, 0, 0, 0)

  // Extension methods for enhanced functionality
  extension (richGL: RichOpenGLBinding)
    def saveState(): Unit =
      lastProgram = richGL.openGLParameters(GL_CURRENT_PROGRAM)
      lastVertexAttributes = richGL.vertexAttributes
      lastScissorTest = gl.glIsEnabled(GL_SCISSOR_TEST)
      lastStencilTest = gl.glIsEnabled(GL_STENCIL_TEST)
      lastDepthTest = gl.glIsEnabled(GL_DEPTH_TEST)
      lastCullFace = gl.glIsEnabled(GL_CULL_FACE)
      lastBlend = gl.glIsEnabled(GL_BLEND)
      lastFrontFace = richGL.openGLParameters(GL_FRONT_FACE)
      lastColorWriteMask = richGL.colorWriteMask
      lastArrayBufferBinding = richGL.openGLParameters(GL_ARRAY_BUFFER_BINDING)
      lastElementArrayBufferBinding = richGL.openGLParameters(GL_ELEMENT_ARRAY_BUFFER_BINDING)
      lastTexture1Binding2D = richGL.textureBinding2D(GL_TEXTURE1)
      lastTexture0Binding2D = richGL.textureBinding2D(GL_TEXTURE0)
      lastActiveTexture = richGL.openGLParameters(GL_ACTIVE_TEXTURE)
      lastBlending = richGL.blendFunction
      lastFrameBufferBinding = richGL.openGLParameters(GL_FRAMEBUFFER_BINDING)
      lastViewPort = richGL.viewPort
      isSaved = true
    
    def restoreState(): Unit =
      if !isSaved then
        throw new IllegalStateException(s"The profile=($this) state is not saved yet.")
      
      gl.glUseProgram(lastProgram)
      richGL.vertexAttributes = lastVertexAttributes
      richGL.setCapabilityEnabled(GL_SCISSOR_TEST, lastScissorTest)
      richGL.setCapabilityEnabled(GL_STENCIL_TEST, lastStencilTest)
      richGL.setCapabilityEnabled(GL_DEPTH_TEST, lastDepthTest)
      richGL.setCapabilityEnabled(GL_CULL_FACE, lastCullFace)
      richGL.setCapabilityEnabled(GL_BLEND, lastBlend)
      gl.glFrontFace(lastFrontFace)
      richGL.colorWriteMask = lastColorWriteMask
      gl.glBindBuffer(GL_ARRAY_BUFFER, lastArrayBufferBinding)
      gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lastElementArrayBufferBinding)
      richGL.activeAndBinding2DTexture(GL_TEXTURE0, lastTexture0Binding2D)
      richGL.activeAndBinding2DTexture(GL_TEXTURE1, lastTexture1Binding2D)
      gl.glActiveTexture(lastActiveTexture)
      richGL.blendFunction = lastBlending
      gl.glBindFramebuffer(GL_FRAMEBUFFER, lastFrameBufferBinding)
      richGL.viewPort = lastViewPort

  def save(): Unit =
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    richGL.saveState()

  def restore(): Unit =
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    richGL.restoreState()
