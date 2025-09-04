package moe.brianhsu.live2d.usecase.renderer.opengl

import moe.brianhsu.live2d.enitiy.opengl.{OpenGLBinding, RichOpenGLBinding}
import moe.brianhsu.live2d.usecase.renderer.opengl.cache.OffscreenRenderCache

object OffscreenFrame:
  private var offscreenFrame: Map[OpenGLBinding, OffscreenFrame] = Map.empty
  private var renderCache: Map[OpenGLBinding, OffscreenRenderCache] = Map.empty

  def getInstance(displayBufferWidth: Int, displayBufferHeight: Int)(using gl: OpenGLBinding): OffscreenFrame =
    offscreenFrame.get(gl) match
      case Some(offscreenFrame) => offscreenFrame
      case None =>
        val (colorTextureBufferId, frameBufferId) = createColorTextureBufferAndFrameBuffer(displayBufferWidth, displayBufferHeight)
        offscreenFrame += (gl -> new OffscreenFrame(colorTextureBufferId, frameBufferId))
        offscreenFrame(gl)

  /**
   * 获取优化的OffscreenFrame实例（使用缓存）
   */
  def getCachedInstance(displayBufferWidth: Int, displayBufferHeight: Int)(using gl: OpenGLBinding): OffscreenFrame = {
    val cache = getOrCreateRenderCache
    cache.getOrCreateFBO(displayBufferWidth, displayBufferHeight)
  }

  /**
   * 获取或创建渲染缓存
   */
  def getOrCreateRenderCache(using gl: OpenGLBinding): OffscreenRenderCache = {
    renderCache.get(gl) match
      case Some(cache) => cache
      case None =>
        val cache = new OffscreenRenderCache
        renderCache += (gl -> cache)
        cache
  }

  /**
   * 清理所有缓存
   */
  def clearAllCaches(): Unit = {
    renderCache.values.foreach(_.clearCache())
    renderCache = Map.empty
    offscreenFrame = Map.empty
  }

  protected def createColorTextureBufferAndFrameBuffer(displayBufferWidth: Int, displayBufferHeight: Int)(using gl: OpenGLBinding): (Int, Int) =
    import gl.constants._
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    
    // Save current OpenGL state
    val originalTextureBinding = richGL.openGLParameters(GL_TEXTURE_BINDING_2D)
    val originalFrameBuffer = richGL.openGLParameters(GL_FRAMEBUFFER_BINDING)
    
    try {
      // Create color texture
      val colorTextureBufferId = richGL.generateTextures(1).head
      gl.glBindTexture(GL_TEXTURE_2D, colorTextureBufferId)
      gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, displayBufferWidth, displayBufferHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
      
      // Create framebuffer
      val frameBufferId = richGL.generateFrameBuffers(1).head
      gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
      gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureBufferId, 0)
      
      // Verify FBO completeness
      val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
      if (status != GL_FRAMEBUFFER_COMPLETE) {
        throw new RuntimeException(s"Framebuffer is not complete: status = $status")
      }
      
      (colorTextureBufferId, frameBufferId)
    } finally {
      // Restore OpenGL state
      gl.glBindTexture(GL_TEXTURE_2D, originalTextureBinding)
      gl.glBindFramebuffer(GL_FRAMEBUFFER, originalFrameBuffer)
    }

class OffscreenFrame(val colorTextureBufferId: Int, val frameBufferId: Int)(using gl: OpenGLBinding):
  import gl.constants._

  private var originalFrameBufferId: Int = 0
  private var originalViewport: (Int, Int, Int, Int) = (0, 0, 0, 0)
  private var isInitialized: Boolean = true

  def beginDraw(currentFrameBufferId: Int): Unit =
    if (!isInitialized) {
      throw new RuntimeException("OffscreenFrame is not properly initialized")
    }
    
    this.originalFrameBufferId = currentFrameBufferId
    
    // Save current viewport
    val viewport = new Array[Int](4)
    gl.glGetIntegerv(GL_VIEWPORT, viewport)
    this.originalViewport = (viewport(0), viewport(1), viewport(2), viewport(3))

    gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
    gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    
    // Verify FBO status
    val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
    if (status != GL_FRAMEBUFFER_COMPLETE) {
      isInitialized = false
      throw new RuntimeException(s"Framebuffer is not complete during beginDraw: status = $status")
    }

  def endDraw(): Unit =
    try {
      // Restore original framebuffer
      gl.glBindFramebuffer(GL_FRAMEBUFFER, this.originalFrameBufferId)
      
      // Restore original viewport
      gl.glViewport(originalViewport(0), originalViewport(1), originalViewport(2), originalViewport(3))
    } catch {
      case e: Exception =>
        isInitialized = false
        throw new RuntimeException("Failed to restore OpenGL state during endDraw", e)
    }

  def isValid: Boolean = isInitialized && {
    try {
      gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
      val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
      gl.glBindFramebuffer(GL_FRAMEBUFFER, 0)
      status == GL_FRAMEBUFFER_COMPLETE
    } catch {
      case _: Exception => false
    }
  }
