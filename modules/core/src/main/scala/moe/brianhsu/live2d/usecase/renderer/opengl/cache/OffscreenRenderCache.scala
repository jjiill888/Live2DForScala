package moe.brianhsu.live2d.usecase.renderer.opengl.cache

import moe.brianhsu.live2d.enitiy.opengl.{OpenGLBinding, RichOpenGLBinding}
import moe.brianhsu.live2d.usecase.renderer.opengl.OffscreenFrame

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable

/**
 * Offscreen Rendering State Cache Manager
 * 
 * Main optimization features:
 * 1. FBO Cache Pool - Avoid duplicate FBO creation
 * 2. OpenGL State Cache - Reduce state switching overhead
 * 3. Texture Cache - Reuse loaded textures
 * 4. Viewport Cache - Avoid duplicate viewport settings
 */
class OffscreenRenderCache(using gl: OpenGLBinding) {
  import gl.constants._

  // FBO cache pool - cache FBOs by size
  private val fboCache = new ConcurrentHashMap[String, OffscreenFrame]()
  
  // OpenGL state cache
  private val stateCache = new OpenGLStateCache
  
  // Texture cache
  private val textureCache = new TextureCache
  
  // Viewport cache
  private var cachedViewport: Option[(Int, Int, Int, Int)] = None
  private var currentViewport: (Int, Int, Int, Int) = (0, 0, 0, 0)

  /**
   * Get or create FBO with cache reuse support
   */
  def getOrCreateFBO(width: Int, height: Int): OffscreenFrame = {
    val key = s"${width}x${height}"
    
    fboCache.computeIfAbsent(key, _ => {
      createOptimizedFBO(width, height)
    })
  }

  /**
   * Create optimized FBO, reduce OpenGL calls
   */
  private def createOptimizedFBO(width: Int, height: Int): OffscreenFrame = {
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    
    // Use state cache to reduce OpenGL queries
    val originalState = stateCache.saveCurrentState()
    
    try {
      // Batch create textures and FBO
      val colorTextureBufferId = richGL.generateTextures(1).head
      val frameBufferId = richGL.generateFrameBuffers(1).head
      
      // Optimize texture setup - batch parameter setting
      gl.glBindTexture(GL_TEXTURE_2D, colorTextureBufferId)
      gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
      
      // Batch set texture parameters
      val textureParams = Array(
        (GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE),
        (GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE),
        (GL_TEXTURE_MIN_FILTER, GL_LINEAR),
        (GL_TEXTURE_MAG_FILTER, GL_LINEAR)
      )
      
      textureParams.foreach { case (param, value) =>
        gl.glTexParameteri(GL_TEXTURE_2D, param, value)
      }
      
      // Create and configure FBO
      gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
      gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureBufferId, 0)
      
      // Verify FBO completeness
      val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
      if (status != GL_FRAMEBUFFER_COMPLETE) {
        throw new RuntimeException(s"Framebuffer is not complete: status = $status")
      }
      
      new CachedOffscreenFrame(colorTextureBufferId, frameBufferId, width, height, this)
      
    } finally {
      // Restore state
      stateCache.restoreState(originalState)
    }
  }

  /**
   * Optimized viewport setting with cache support
   */
  def setViewport(x: Int, y: Int, width: Int, height: Int): Unit = {
    val newViewport = (x, y, width, height)
    if (cachedViewport.isEmpty || cachedViewport.get != newViewport) {
      gl.glViewport(x, y, width, height)
      cachedViewport = Some(newViewport)
      currentViewport = newViewport
    }
  }

  /**
   * Get current viewport
   */
  def getCurrentViewport: (Int, Int, Int, Int) = currentViewport

  /**
   * Get texture cache
   */
  def getTextureCache: TextureCache = textureCache

  /**
   * Get state cache
   */
  def getStateCache: OpenGLStateCache = stateCache

  /**
   * Clear cache
   */
  def clearCache(): Unit = {
    // Clear FBO cache
    fboCache.values().forEach { frame =>
      frame match {
        case cached: CachedOffscreenFrame => cached.cleanup()
        case _ => // Regular OffscreenFrame doesn't need special cleanup
      }
    }
    fboCache.clear()
    
    // Clear other caches
    textureCache.clearCache()
    stateCache.clearCache()
    cachedViewport = None
  }

  /**
   * Get cache statistics
   */
  def getCacheStats: CacheStats = {
    CacheStats(
      fboCount = fboCache.size(),
      textureCount = textureCache.getCacheSize,
      stateCacheSize = stateCache.getCacheSize
    )
  }
}

/**
 * Cache statistics
 */
case class CacheStats(
  fboCount: Int,
  textureCount: Int,
  stateCacheSize: Int
)

/**
 * Cached OffscreenFrame implementation
 */
class CachedOffscreenFrame(
  colorTextureBufferId: Int, 
  frameBufferId: Int, 
  width: Int, 
  height: Int,
  cache: OffscreenRenderCache
)(using gl: OpenGLBinding) extends OffscreenFrame(colorTextureBufferId, frameBufferId) {
  import gl.constants._
  
  private var isActive = false
  private var savedState: Option[OpenGLState] = None

  override def beginDraw(currentFrameBufferId: Int): Unit = {
    if (isActive) {
      throw new RuntimeException("OffscreenFrame is already active")
    }
    
    isActive = true
    
    // Use cached state management
    savedState = Some(cache.getStateCache.saveCurrentState())
    
    // Set viewport (using cache optimization)
    cache.setViewport(0, 0, width, height)
    
    // Bind FBO
    gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
    gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    
    // Verify FBO status
    val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
    if (status != GL_FRAMEBUFFER_COMPLETE) {
      throw new RuntimeException(s"Framebuffer is not complete during beginDraw: status = $status")
    }
  }

  override def endDraw(): Unit = {
    if (!isActive) {
      throw new RuntimeException("OffscreenFrame is not active")
    }
    
    try {
      // Use cached state restoration
      savedState.foreach(cache.getStateCache.restoreState)
      savedState = None
      
    } catch {
      case e: Exception =>
        throw new RuntimeException("Failed to restore OpenGL state during endDraw", e)
    } finally {
      isActive = false
    }
  }

  override def isValid: Boolean = {
    if (!isActive) {
      try {
        gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)
        val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0)
        status == GL_FRAMEBUFFER_COMPLETE
      } catch {
        case _: Exception => false
      }
          } else {
        true // If in use, consider it valid
      }
  }

  /**
   * Cleanup resources
   */
  def cleanup(): Unit = {
    if (isActive) {
      endDraw()
    }
    
    try {
      // Delete textures and FBO
      gl.glDeleteTextures(1, Array(colorTextureBufferId))
      gl.glDeleteFramebuffers(1, Array(frameBufferId))
    } catch {
      case e: Exception =>
        System.err.println(s"Warning: Failed to cleanup FBO resources: ${e.getMessage}")
    }
  }
}
