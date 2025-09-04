package moe.brianhsu.live2d.usecase.renderer.opengl.cache

import moe.brianhsu.live2d.enitiy.opengl.{OpenGLBinding, RichOpenGLBinding}
import moe.brianhsu.live2d.enitiy.opengl.texture.TextureInfo

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable

/**
 * Texture Cache Manager
 * 
 * Optimization features:
 * 1. Texture loading cache, avoid duplicate loading
 * 2. Texture binding cache, reduce binding overhead
 * 3. Texture parameter cache, avoid duplicate settings
 * 4. LRU cache strategy, auto-cleanup unused textures
 */
class TextureCache(using gl: OpenGLBinding) {
  import gl.constants._

  // 纹理缓存 - 文件路径 -> 纹理信息
  private val textureCache = new ConcurrentHashMap[String, CachedTextureInfo]()
  
  // 当前绑定的纹理
  private var currentBoundTexture: Option[Int] = None
  
  // 纹理参数缓存
  private val textureParamsCache = new ConcurrentHashMap[Int, TextureParams]()
  
  // 访问时间跟踪（用于LRU）
  private val accessTimes = mutable.Map[String, Long]()
  
  // 最大缓存大小
  private val maxCacheSize = 100
  
  // 纹理参数默认值
  private val defaultTextureParams = TextureParams(
    wrapS = GL_CLAMP_TO_EDGE,
    wrapT = GL_CLAMP_TO_EDGE,
    minFilter = GL_LINEAR,
    magFilter = GL_LINEAR
  )

  /**
   * 获取或加载纹理
   */
  def getOrLoadTexture(textureFile: File): TextureInfo = {
    val key = textureFile.getAbsolutePath
    
    // 更新访问时间
    accessTimes(key) = System.currentTimeMillis()
    
    textureCache.computeIfAbsent(key, _ => {
      // 检查缓存大小，必要时清理
      if (textureCache.size() >= maxCacheSize) {
        cleanupLRUTextures()
      }
      
      loadTexture(textureFile)
    }).textureInfo
  }

  /**
   * 加载纹理
   */
  private def loadTexture(textureFile: File): CachedTextureInfo = {
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    
    // 生成纹理ID
    val textureId = richGL.generateTextures(1).head
    
    // 绑定纹理
    bindTexture(textureId)
    
    // 设置默认纹理参数
    setTextureParams(textureId, defaultTextureParams)
    
    // 这里应该调用实际的纹理加载逻辑
    // 由于原始代码中的纹理加载逻辑比较复杂，这里提供一个框架
    // 实际实现需要根据具体的纹理加载器来调整
    
    val textureInfo = TextureInfo(
      textureId = textureId,
      width = 0, // 需要从实际加载的纹理获取
      height = 0  // 需要从实际加载的纹理获取
    )
    
    CachedTextureInfo(textureInfo, System.currentTimeMillis())
  }

  /**
   * 绑定纹理（带缓存优化）
   */
  def bindTexture(textureId: Int): Unit = {
    if (currentBoundTexture.isEmpty || currentBoundTexture.get != textureId) {
      gl.glBindTexture(GL_TEXTURE_2D, textureId)
      currentBoundTexture = Some(textureId)
    }
  }

  /**
   * 设置纹理参数（带缓存优化）
   */
  def setTextureParams(textureId: Int, params: TextureParams): Unit = {
    val cachedParams = textureParamsCache.get(textureId)
    
    if (cachedParams == null || cachedParams != params) {
      bindTexture(textureId)
      
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, params.wrapS)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, params.wrapT)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, params.minFilter)
      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, params.magFilter)
      
      textureParamsCache.put(textureId, params)
    }
  }

  /**
   * 批量设置纹理参数
   */
  def setTextureParams(textureId: Int, 
                      wrapS: Int = GL_CLAMP_TO_EDGE,
                      wrapT: Int = GL_CLAMP_TO_EDGE,
                      minFilter: Int = GL_LINEAR,
                      magFilter: Int = GL_LINEAR): Unit = {
    val params = TextureParams(wrapS, wrapT, minFilter, magFilter)
    setTextureParams(textureId, params)
  }

  /**
   * 清理LRU纹理
   */
  private def cleanupLRUTextures(): Unit = {
    if (textureCache.size() < maxCacheSize) return
    
    // 计算需要清理的数量
    val cleanupCount = textureCache.size() - (maxCacheSize * 0.8).toInt
    
    // 按访问时间排序，清理最旧的
    val sortedByTime = accessTimes.toSeq.sortBy(_._2)
    
    for (i <- 0 until cleanupCount) {
      val (key, _) = sortedByTime(i)
      val cachedTexture = textureCache.remove(key)
      
      if (cachedTexture != null) {
        // 删除OpenGL纹理
        gl.glDeleteTextures(1, Array(cachedTexture.textureInfo.textureId))
        
        // 清理参数缓存
        textureParamsCache.remove(cachedTexture.textureInfo.textureId)
      }
      
      accessTimes.remove(key)
    }
  }

  /**
   * 预加载纹理
   */
  def preloadTextures(textureFiles: Seq[File]): Unit = {
    textureFiles.foreach { file =>
      if (!textureCache.containsKey(file.getAbsolutePath)) {
        getOrLoadTexture(file)
      }
    }
  }

  /**
   * 获取缓存大小
   */
  def getCacheSize: Int = textureCache.size()

  /**
   * 获取缓存统计信息
   */
  def getCacheStats: TextureCacheStats = {
    val totalSize = textureCache.size()
    val paramCacheSize = textureParamsCache.size()
    val oldestAccess = if (accessTimes.nonEmpty) accessTimes.values.min else 0L
    val newestAccess = if (accessTimes.nonEmpty) accessTimes.values.max else 0L
    
    TextureCacheStats(
      textureCount = totalSize,
      paramCacheSize = paramCacheSize,
      oldestAccess = oldestAccess,
      newestAccess = newestAccess,
      currentBoundTexture = currentBoundTexture
    )
  }

  /**
   * 清理所有缓存
   */
  def clearCache(): Unit = {
    // 删除所有OpenGL纹理
    textureCache.values().forEach { cachedTexture =>
      gl.glDeleteTextures(1, Array(cachedTexture.textureInfo.textureId))
    }
    
    textureCache.clear()
    textureParamsCache.clear()
    accessTimes.clear()
    currentBoundTexture = None
  }

  /**
   * 检查纹理是否在缓存中
   */
  def isTextureCached(textureFile: File): Boolean = {
    textureCache.containsKey(textureFile.getAbsolutePath)
  }

  /**
   * 强制重新加载纹理
   */
  def reloadTexture(textureFile: File): TextureInfo = {
    val key = textureFile.getAbsolutePath
    val oldCached = textureCache.remove(key)
    
    if (oldCached != null) {
      gl.glDeleteTextures(1, Array(oldCached.textureInfo.textureId))
      textureParamsCache.remove(oldCached.textureInfo.textureId)
    }
    
    accessTimes.remove(key)
    getOrLoadTexture(textureFile)
  }
}

/**
 * 缓存的纹理信息
 */
case class CachedTextureInfo(
  textureInfo: TextureInfo,
  loadTime: Long
)

/**
 * 纹理参数
 */
case class TextureParams(
  wrapS: Int,
  wrapT: Int,
  minFilter: Int,
  magFilter: Int
)

/**
 * 纹理缓存统计信息
 */
case class TextureCacheStats(
  textureCount: Int,
  paramCacheSize: Int,
  oldestAccess: Long,
  newestAccess: Long,
  currentBoundTexture: Option[Int]
)
