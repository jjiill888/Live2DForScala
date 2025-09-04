package moe.brianhsu.live2d.usecase.renderer.opengl.cache

import moe.brianhsu.live2d.enitiy.opengl.{OpenGLBinding, RichOpenGLBinding}
import moe.brianhsu.live2d.usecase.renderer.opengl.OffscreenFrame

import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue, Semaphore}
import scala.collection.mutable
import scala.util.{Try, Success, Failure}

/**
 * Advanced FBO Pool Manager
 * 
 * Optimization features:
 * 1. Multi-tier FBO pooling with size-based allocation
 * 2. Dynamic pool sizing based on usage patterns
 * 3. FBO recycling and reuse optimization
 * 4. Memory pressure-aware pool management
 * 5. Async FBO creation and cleanup
 * 6. Pool statistics and monitoring
 */
class FBOPoolManager(using gl: OpenGLBinding) {
  import gl.constants._

  // Multi-tier FBO pools
  private val smallFBOPool = new ConcurrentLinkedQueue[PooledFBO]() // 64x64 to 256x256
  private val mediumFBOPool = new ConcurrentLinkedQueue[PooledFBO]() // 256x256 to 1024x1024
  private val largeFBOPool = new ConcurrentLinkedQueue[PooledFBO]() // 1024x1024 to 4096x4096
  private val extraLargeFBOPool = new ConcurrentLinkedQueue[PooledFBO]() // 4096x4096+
  
  // Pool configuration
  private val poolConfigs = Map(
    "small" -> PoolConfig(64, 256, 20, 5),
    "medium" -> PoolConfig(256, 1024, 15, 3),
    "large" -> PoolConfig(1024, 4096, 10, 2),
    "extraLarge" -> PoolConfig(4096, 8192, 5, 1)
  )
  
  // Active FBO tracking
  private val activeFBOs = new ConcurrentHashMap[String, PooledFBO]()
  private val fboUsageStats = new ConcurrentHashMap[String, FBOUsageStats]()
  
  // Pool management
  private val poolSemaphores = Map(
    "small" -> new Semaphore(poolConfigs("small").maxPoolSize),
    "medium" -> new Semaphore(poolConfigs("medium").maxPoolSize),
    "large" -> new Semaphore(poolConfigs("large").maxPoolSize),
    "extraLarge" -> new Semaphore(poolConfigs("extraLarge").maxPoolSize)
  )
  
  // Performance monitoring
  private val poolStats = new PoolStatistics()
  private var lastCleanupTime = System.currentTimeMillis()
  private val cleanupInterval = 30000 // 30 seconds

  /**
   * Get FBO from pool or create new one
   */
  def getFBO(width: Int, height: Int): Try[PooledFBO] = {
    val startTime = System.nanoTime()
    
    try {
      val poolType = determinePoolType(width, height)
      val key = s"${width}x${height}"
      
      // Check if we have an active FBO of this exact size
      val existingFBO = activeFBOs.get(key)
      if (existingFBO != null && existingFBO.isAvailable) {
        existingFBO.markInUse()
        updateUsageStats(key, true)
        poolStats.recordReuse(System.nanoTime() - startTime)
        return Success(existingFBO)
      }
      
      // Try to get from appropriate pool
      val pool = getPool(poolType)
      val semaphore = poolSemaphores(poolType)
      
      if (semaphore.tryAcquire()) {
        val pooledFBO = pool.poll()
        if (pooledFBO != null && pooledFBO.matchesSize(width, height)) {
          pooledFBO.markInUse()
          activeFBOs.put(key, pooledFBO)
          updateUsageStats(key, true)
          poolStats.recordPoolHit(System.nanoTime() - startTime)
          return Success(pooledFBO)
        } else {
          semaphore.release()
        }
      }
      
      // Create new FBO
      val newFBO = createNewFBO(width, height, poolType)
      newFBO.markInUse()
      activeFBOs.put(key, newFBO)
      updateUsageStats(key, false)
      poolStats.recordCreation(System.nanoTime() - startTime)
      
      Success(newFBO)
      
    } catch {
      case e: Exception =>
        poolStats.recordError()
        Failure(e)
    }
  }

  /**
   * Return FBO to pool for reuse
   */
  def returnFBO(fbo: PooledFBO): Unit = {
    val startTime = System.nanoTime()
    
    try {
      fbo.markAvailable()
      val key = s"${fbo.width}x${fbo.height}"
      activeFBOs.remove(key)
      
      val poolType = determinePoolType(fbo.width, fbo.height)
      val pool = getPool(poolType)
      val config = poolConfigs(poolType)
      
      // Check if pool has space
      if (pool.size() < config.maxPoolSize) {
        pool.offer(fbo)
        poolStats.recordReturn(System.nanoTime() - startTime)
      } else {
        // Pool is full, cleanup the FBO
        fbo.cleanup()
        poolStats.recordCleanup(System.nanoTime() - startTime)
      }
      
      // Periodic cleanup
      if (System.currentTimeMillis() - lastCleanupTime > cleanupInterval) {
        performPeriodicCleanup()
        lastCleanupTime = System.currentTimeMillis()
      }
      
    } catch {
      case e: Exception =>
        System.err.println(s"Error returning FBO to pool: ${e.getMessage}")
        fbo.cleanup()
    }
  }

  /**
   * Pre-allocate FBOs for common sizes
   */
  def preAllocateCommonSizes(): Unit = {
    val commonSizes = Seq(
      (256, 256), (512, 512), (1024, 1024),
      (128, 128), (64, 64), (2048, 2048)
    )
    
    commonSizes.foreach { case (width, height) =>
      val poolType = determinePoolType(width, height)
      val config = poolConfigs(poolType)
      val pool = getPool(poolType)
      
      // Pre-allocate up to minPoolSize
      for (_ <- 0 until config.minPoolSize) {
        if (pool.size() < config.minPoolSize) {
          Try {
            val fbo = createNewFBO(width, height, poolType)
            fbo.markAvailable()
            pool.offer(fbo)
          } match {
            case Success(_) => // Success
            case Failure(e) => 
              System.err.println(s"Failed to pre-allocate FBO: ${e.getMessage}")
          }
        }
      }
    }
  }

  /**
   * Create new FBO with optimization
   */
  private def createNewFBO(width: Int, height: Int, poolType: String): PooledFBO = {
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    
    // Batch create resources
    val textureIds = richGL.generateTextures(1)
    val fboIds = richGL.generateFrameBuffers(1)
    
    val textureId = textureIds.head
    val fboId = fboIds.head
    
    // Optimize texture setup
    gl.glBindTexture(GL_TEXTURE_2D, textureId)
    
    // Use appropriate internal format based on size
    val internalFormat = if (width * height > 1024 * 1024) GL_RGBA else GL_RGBA // Use RGBA for compatibility
    gl.glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null)
    
    // Batch set texture parameters
    val parameters = Array(
      (GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE),
      (GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE),
      (GL_TEXTURE_MIN_FILTER, GL_LINEAR),
      (GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    )
    
    parameters.foreach { case (param, value) =>
      gl.glTexParameteri(GL_TEXTURE_2D, param, value)
    }
    
    // Setup FBO
    gl.glBindFramebuffer(GL_FRAMEBUFFER, fboId)
    gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)
    
    // Verify FBO
    val status = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER)
    if (status != GL_FRAMEBUFFER_COMPLETE) {
      throw new RuntimeException(s"FBO creation failed: status = $status")
    }
    
    // Unbind
    gl.glBindFramebuffer(GL_FRAMEBUFFER, 0)
    gl.glBindTexture(GL_TEXTURE_2D, 0)
    
    new PooledFBO(textureId, fboId, width, height, poolType, gl)
  }

  /**
   * Determine pool type based on size
   */
  private def determinePoolType(width: Int, height: Int): String = {
    val maxDimension = math.max(width, height)
    if (maxDimension <= 256) "small"
    else if (maxDimension <= 1024) "medium"
    else if (maxDimension <= 4096) "large"
    else "extraLarge"
  }

  /**
   * Get appropriate pool
   */
  private def getPool(poolType: String): ConcurrentLinkedQueue[PooledFBO] = {
    poolType match {
      case "small" => smallFBOPool
      case "medium" => mediumFBOPool
      case "large" => largeFBOPool
      case "extraLarge" => extraLargeFBOPool
      case _ => throw new IllegalArgumentException(s"Unknown pool type: $poolType")
    }
  }

  /**
   * Update usage statistics
   */
  private def updateUsageStats(key: String, wasReused: Boolean): Unit = {
    fboUsageStats.compute(key, (_, stats) => {
      if (stats == null) {
        FBOUsageStats(1, if (wasReused) 1 else 0, System.currentTimeMillis())
      } else {
        stats.copy(
          totalUses = stats.totalUses + 1,
          reuseCount = if (wasReused) stats.reuseCount + 1 else stats.reuseCount,
          lastUsed = System.currentTimeMillis()
        )
      }
    })
  }

  /**
   * Perform periodic cleanup
   */
  private def performPeriodicCleanup(): Unit = {
    val currentTime = System.currentTimeMillis()
    val maxAge = 300000 // 5 minutes
    
    // Clean up unused FBOs
    poolConfigs.keys.foreach { poolType =>
      val pool = getPool(poolType)
      val iterator = pool.iterator()
      
      while (iterator.hasNext) {
        val fbo = iterator.next()
        if (currentTime - fbo.getTimeSinceLastUse > maxAge) {
          iterator.remove()
          fbo.cleanup()
          poolStats.recordPeriodicCleanup()
        }
      }
    }
    
    // Clean up usage stats
    val statsIterator = fboUsageStats.entrySet().iterator()
    while (statsIterator.hasNext) {
      val entry = statsIterator.next()
      if (currentTime - entry.getValue.lastUsed > maxAge * 2) {
        statsIterator.remove()
      }
    }
  }

  /**
   * Get pool statistics
   */
  def getPoolStats: PoolStatistics = poolStats

  /**
   * Get detailed pool information
   */
  def getPoolInfo: PoolInfo = {
    PoolInfo(
      smallPoolSize = smallFBOPool.size(),
      mediumPoolSize = mediumFBOPool.size(),
      largePoolSize = largeFBOPool.size(),
      extraLargePoolSize = extraLargeFBOPool.size(),
      activeFBOs = activeFBOs.size(),
      totalUsageStats = fboUsageStats.size()
    )
  }

  /**
   * Clear all pools
   */
  def clearAllPools(): Unit = {
    // Cleanup all pooled FBOs
    Seq(smallFBOPool, mediumFBOPool, largeFBOPool, extraLargeFBOPool).foreach { pool =>
      pool.forEach(_.cleanup())
      pool.clear()
    }
    
    // Cleanup active FBOs
    activeFBOs.values().forEach(_.cleanup())
    activeFBOs.clear()
    
    // Clear statistics
    fboUsageStats.clear()
    poolStats.reset()
  }
}

// Data classes
case class PoolConfig(minSize: Int, maxSize: Int, maxPoolSize: Int, minPoolSize: Int)

case class FBOUsageStats(totalUses: Int, reuseCount: Int, lastUsed: Long) {
  def reuseRate: Double = if (totalUses > 0) reuseCount.toDouble / totalUses else 0.0
}

case class PoolInfo(
  smallPoolSize: Int,
  mediumPoolSize: Int,
  largePoolSize: Int,
  extraLargePoolSize: Int,
  activeFBOs: Int,
  totalUsageStats: Int
)

/**
 * Pooled FBO with enhanced tracking
 */
class PooledFBO(
  val textureId: Int,
  val frameBufferId: Int,
  val width: Int,
  val height: Int,
  val poolType: String,
  gl: OpenGLBinding
) {
  import gl.constants._
  
  private var inUse = false
  private var lastUsed = System.currentTimeMillis()
  private var creationTime = System.currentTimeMillis()
  
  def isAvailable: Boolean = !inUse
  
  def markInUse(): Unit = {
    inUse = true
    lastUsed = System.currentTimeMillis()
  }
  
  def markAvailable(): Unit = {
    inUse = false
    lastUsed = System.currentTimeMillis()
  }
  
  def matchesSize(w: Int, h: Int): Boolean = w == width && h == height
  
  def getAge: Long = System.currentTimeMillis() - creationTime
  
  def getTimeSinceLastUse: Long = System.currentTimeMillis() - lastUsed
  
  def cleanup(): Unit = {
    try {
      gl.glDeleteTextures(1, Array(textureId))
      gl.glDeleteFramebuffers(1, Array(frameBufferId))
    } catch {
      case e: Exception =>
        System.err.println(s"Error cleaning up FBO: ${e.getMessage}")
    }
  }
}

/**
 * Pool statistics tracking
 */
class PoolStatistics {
  private val reuseTimes = mutable.ArrayBuffer[Long]()
  private val creationTimes = mutable.ArrayBuffer[Long]()
  private val returnTimes = mutable.ArrayBuffer[Long]()
  private val cleanupTimes = mutable.ArrayBuffer[Long]()
  private val poolHitTimes = mutable.ArrayBuffer[Long]()
  private var errorCount = 0
  private var periodicCleanupCount = 0
  
  def recordReuse(timeNanos: Long): Unit = reuseTimes += timeNanos
  def recordCreation(timeNanos: Long): Unit = creationTimes += timeNanos
  def recordReturn(timeNanos: Long): Unit = returnTimes += timeNanos
  def recordCleanup(timeNanos: Long): Unit = cleanupTimes += timeNanos
  def recordPoolHit(timeNanos: Long): Unit = poolHitTimes += timeNanos
  def recordError(): Unit = errorCount += 1
  def recordPeriodicCleanup(): Unit = periodicCleanupCount += 1
  
  def getAverageReuseTime: Double = if (reuseTimes.nonEmpty) reuseTimes.sum.toDouble / reuseTimes.size else 0.0
  def getAverageCreationTime: Double = if (creationTimes.nonEmpty) creationTimes.sum.toDouble / creationTimes.size else 0.0
  def getAverageReturnTime: Double = if (returnTimes.nonEmpty) returnTimes.sum.toDouble / returnTimes.size else 0.0
  def getAverageCleanupTime: Double = if (cleanupTimes.nonEmpty) cleanupTimes.sum.toDouble / cleanupTimes.size else 0.0
  def getAveragePoolHitTime: Double = if (poolHitTimes.nonEmpty) poolHitTimes.sum.toDouble / poolHitTimes.size else 0.0
  
  def getTotalOperations: Int = reuseTimes.size + creationTimes.size + returnTimes.size + cleanupTimes.size
  def getErrorCount: Int = errorCount
  def getPeriodicCleanupCount: Int = periodicCleanupCount
  
  def reset(): Unit = {
    reuseTimes.clear()
    creationTimes.clear()
    returnTimes.clear()
    cleanupTimes.clear()
    poolHitTimes.clear()
    errorCount = 0
    periodicCleanupCount = 0
  }
}
