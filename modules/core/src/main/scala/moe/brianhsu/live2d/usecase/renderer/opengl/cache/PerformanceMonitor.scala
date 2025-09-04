package moe.brianhsu.live2d.usecase.renderer.opengl.cache

import moe.brianhsu.live2d.enitiy.opengl.OpenGLBinding

import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable

/**
 * Offscreen Rendering Performance Monitor
 * 
 * Features:
 * 1. Monitor FBO create/destroy count
 * 2. Monitor OpenGL state switch count
 * 3. Monitor texture load/bind count
 * 4. Calculate performance improvement metrics
 */
class PerformanceMonitor(using gl: OpenGLBinding) {
  
  // Performance counters
  private val fboCreateCount = new AtomicLong(0)
  private val fboReuseCount = new AtomicLong(0)
  private val textureLoadCount = new AtomicLong(0)
  private val textureReuseCount = new AtomicLong(0)
  private val stateSwitchCount = new AtomicLong(0)
  private val stateCacheHitCount = new AtomicLong(0)
  private val viewportSetCount = new AtomicLong(0)
  private val viewportCacheHitCount = new AtomicLong(0)
  
  // Time statistics
  private val renderTimes = mutable.ArrayBuffer[Long]()
  private val fboCreateTimes = mutable.ArrayBuffer[Long]()
  private val textureLoadTimes = mutable.ArrayBuffer[Long]()
  
  // Memory usage statistics
  private val memoryUsage = mutable.ArrayBuffer[Long]()
  
  // Start time recording
  private var renderStartTime: Long = 0
  private var fboCreateStartTime: Long = 0
  private var textureLoadStartTime: Long = 0

  /**
   * Start render timing
   */
  def startRenderTiming(): Unit = {
    renderStartTime = System.nanoTime()
  }

  /**
   * End render timing
   */
  def endRenderTiming(): Unit = {
    val duration = System.nanoTime() - renderStartTime
    renderTimes += duration
  }

  /**
   * Record FBO creation
   */
  def recordFBOCreation(isReuse: Boolean): Unit = {
    if (isReuse) {
      fboReuseCount.incrementAndGet()
    } else {
      fboCreateCount.incrementAndGet()
    }
  }

  /**
   * Start FBO creation timing
   */
  def startFBOCreationTiming(): Unit = {
    fboCreateStartTime = System.nanoTime()
  }

  /**
   * End FBO creation timing
   */
  def endFBOCreationTiming(): Unit = {
    val duration = System.nanoTime() - fboCreateStartTime
    fboCreateTimes += duration
  }

  /**
   * Record texture loading
   */
  def recordTextureLoad(isReuse: Boolean): Unit = {
    if (isReuse) {
      textureReuseCount.incrementAndGet()
    } else {
      textureLoadCount.incrementAndGet()
    }
  }

  /**
   * Start texture loading timing
   */
  def startTextureLoadTiming(): Unit = {
    textureLoadStartTime = System.nanoTime()
  }

  /**
   * End texture loading timing
   */
  def endTextureLoadTiming(): Unit = {
    val duration = System.nanoTime() - textureLoadStartTime
    textureLoadTimes += duration
  }

  /**
   * Record state switch
   */
  def recordStateSwitch(isCacheHit: Boolean): Unit = {
    stateSwitchCount.incrementAndGet()
    if (isCacheHit) {
      stateCacheHitCount.incrementAndGet()
    }
  }

  /**
   * Record viewport setting
   */
  def recordViewportSet(isCacheHit: Boolean): Unit = {
    viewportSetCount.incrementAndGet()
    if (isCacheHit) {
      viewportCacheHitCount.incrementAndGet()
    }
  }

  /**
   * Record memory usage
   */
  def recordMemoryUsage(bytes: Long): Unit = {
    memoryUsage += bytes
  }

  /**
   * Get performance statistics report
   */
  def getPerformanceReport: PerformanceReport = {
    val totalFBOOps = fboCreateCount.get() + fboReuseCount.get()
    val totalTextureOps = textureLoadCount.get() + textureReuseCount.get()
    val totalStateOps = stateSwitchCount.get()
    val totalViewportOps = viewportSetCount.get()
    
    PerformanceReport(
      // FBO statistics
      fboCreateCount = fboCreateCount.get(),
      fboReuseCount = fboReuseCount.get(),
      fboReuseRate = if (totalFBOOps > 0) fboReuseCount.get().toDouble / totalFBOOps else 0.0,
      avgFBOCreationTime = if (fboCreateTimes.nonEmpty) fboCreateTimes.sum / fboCreateTimes.length else 0L,
      
      // Texture statistics
      textureLoadCount = textureLoadCount.get(),
      textureReuseCount = textureReuseCount.get(),
      textureReuseRate = if (totalTextureOps > 0) textureReuseCount.get().toDouble / totalTextureOps else 0.0,
      avgTextureLoadTime = if (textureLoadTimes.nonEmpty) textureLoadTimes.sum / textureLoadTimes.length else 0L,
      
      // State statistics
      stateSwitchCount = stateSwitchCount.get(),
      stateCacheHitCount = stateCacheHitCount.get(),
      stateCacheHitRate = if (totalStateOps > 0) stateCacheHitCount.get().toDouble / totalStateOps else 0.0,
      
      // Viewport statistics
      viewportSetCount = viewportSetCount.get(),
      viewportCacheHitCount = viewportCacheHitCount.get(),
      viewportCacheHitRate = if (totalViewportOps > 0) viewportCacheHitCount.get().toDouble / totalViewportOps else 0.0,
      
      // Render statistics
      avgRenderTime = if (renderTimes.nonEmpty) renderTimes.sum / renderTimes.length else 0L,
      minRenderTime = if (renderTimes.nonEmpty) renderTimes.min else 0L,
      maxRenderTime = if (renderTimes.nonEmpty) renderTimes.max else 0L,
      
      // Memory statistics
      avgMemoryUsage = if (memoryUsage.nonEmpty) memoryUsage.sum / memoryUsage.length else 0L,
      maxMemoryUsage = if (memoryUsage.nonEmpty) memoryUsage.max else 0L,
      
      // Overall statistics
      totalOperations = totalFBOOps + totalTextureOps + totalStateOps + totalViewportOps,
      totalCacheHits = fboReuseCount.get() + textureReuseCount.get() + stateCacheHitCount.get() + viewportCacheHitCount.get(),
      overallCacheHitRate = {
        val totalOps = totalFBOOps + totalTextureOps + totalStateOps + totalViewportOps
        if (totalOps > 0) (fboReuseCount.get() + textureReuseCount.get() + stateCacheHitCount.get() + viewportCacheHitCount.get()).toDouble / totalOps else 0.0
      }
    )
  }

  /**
   * Get comprehensive performance metrics
   */
  def getMetrics(): PerformanceMetrics = {
    PerformanceMetrics(
      fboCreateCount = fboCreateCount.get(),
      fboReuseCount = fboReuseCount.get(),
      textureLoadCount = textureLoadCount.get(),
      textureReuseCount = textureReuseCount.get(),
      stateSwitchCount = stateSwitchCount.get(),
      stateCacheHitCount = stateCacheHitCount.get(),
      viewportSetCount = viewportSetCount.get(),
      viewportCacheHitCount = viewportCacheHitCount.get(),
      averageRenderTime = if (renderTimes.nonEmpty) renderTimes.sum.toDouble / renderTimes.size else 0.0,
      averageFBOCreationTime = if (fboCreateTimes.nonEmpty) fboCreateTimes.sum.toDouble / fboCreateTimes.size else 0.0,
      averageTextureLoadTime = if (textureLoadTimes.nonEmpty) textureLoadTimes.sum.toDouble / textureLoadTimes.size else 0.0,
      memoryUsage = if (memoryUsage.nonEmpty) memoryUsage.sum else 0L
    )
  }

  /**
   * Reset all statistics
   */
  def reset(): Unit = {
    fboCreateCount.set(0)
    fboReuseCount.set(0)
    textureLoadCount.set(0)
    textureReuseCount.set(0)
    stateSwitchCount.set(0)
    stateCacheHitCount.set(0)
    viewportSetCount.set(0)
    viewportCacheHitCount.set(0)
    
    renderTimes.clear()
    fboCreateTimes.clear()
    textureLoadTimes.clear()
    memoryUsage.clear()
  }

  /**
   * Get real-time performance metrics
   */
  def getRealtimeMetrics: RealtimeMetrics = {
    RealtimeMetrics(
      currentFBOReuseRate = if (fboCreateCount.get() + fboReuseCount.get() > 0) 
        fboReuseCount.get().toDouble / (fboCreateCount.get() + fboReuseCount.get()) else 0.0,
      currentTextureReuseRate = if (textureLoadCount.get() + textureReuseCount.get() > 0)
        textureReuseCount.get().toDouble / (textureLoadCount.get() + textureReuseCount.get()) else 0.0,
      currentStateCacheHitRate = if (stateSwitchCount.get() > 0)
        stateCacheHitCount.get().toDouble / stateSwitchCount.get() else 0.0,
      currentViewportCacheHitRate = if (viewportSetCount.get() > 0)
        viewportCacheHitCount.get().toDouble / viewportSetCount.get() else 0.0,
      lastRenderTime = if (renderTimes.nonEmpty) renderTimes.last else 0L,
      lastMemoryUsage = if (memoryUsage.nonEmpty) memoryUsage.last else 0L
    )
  }
}

/**
 * Performance report
 */
case class PerformanceReport(
  // FBO statistics
  fboCreateCount: Long,
  fboReuseCount: Long,
  fboReuseRate: Double,
  avgFBOCreationTime: Long,
  
  // Texture statistics
  textureLoadCount: Long,
  textureReuseCount: Long,
  textureReuseRate: Double,
  avgTextureLoadTime: Long,
  
  // State statistics
  stateSwitchCount: Long,
  stateCacheHitCount: Long,
  stateCacheHitRate: Double,
  
  // Viewport statistics
  viewportSetCount: Long,
  viewportCacheHitCount: Long,
  viewportCacheHitRate: Double,
  
  // Render statistics
  avgRenderTime: Long,
  minRenderTime: Long,
  maxRenderTime: Long,
  
  // Memory statistics
  avgMemoryUsage: Long,
  maxMemoryUsage: Long,
  
  // Overall statistics
  totalOperations: Long,
  totalCacheHits: Long,
  overallCacheHitRate: Double
) {
  
  /**
   * Format report as string
   */
  def formatReport: String = {
    s"""
    |=== Offscreen Rendering Performance Report ===
    |FBO Statistics:
    |  Create Count: $fboCreateCount
    |  Reuse Count: $fboReuseCount
    |  Reuse Rate: ${String.format("%.2f%%", fboReuseRate * 100)}
    |  Average Creation Time: ${avgFBOCreationTime / 1000000.0}ms
    |
    |Texture Statistics:
    |  Load Count: $textureLoadCount
    |  Reuse Count: $textureReuseCount
    |  Reuse Rate: ${String.format("%.2f%%", textureReuseRate * 100)}
    |  Average Load Time: ${avgTextureLoadTime / 1000000.0}ms
    |
    |State Statistics:
    |  Switch Count: $stateSwitchCount
    |  Cache Hits: $stateCacheHitCount
    |  Cache Hit Rate: ${String.format("%.2f%%", stateCacheHitRate * 100)}
    |
    |Viewport Statistics:
    |  Set Count: $viewportSetCount
    |  Cache Hits: $viewportCacheHitCount
    |  Cache Hit Rate: ${String.format("%.2f%%", viewportCacheHitRate * 100)}
    |
    |Render Statistics:
    |  Average Render Time: ${avgRenderTime / 1000000.0}ms
    |  Min Render Time: ${minRenderTime / 1000000.0}ms
    |  Max Render Time: ${maxRenderTime / 1000000.0}ms
    |
    |Memory Statistics:
    |  Average Memory Usage: ${avgMemoryUsage / 1024.0 / 1024.0}MB
    |  Max Memory Usage: ${maxMemoryUsage / 1024.0 / 1024.0}MB
    |
    |Overall Statistics:
    |  Total Operations: $totalOperations
    |  Total Cache Hits: $totalCacheHits
    |  Overall Cache Hit Rate: ${String.format("%.2f%%", overallCacheHitRate * 100)}
    |===============================================
    """.stripMargin
  }
}

/**
 * Performance metrics for the new system
 */
case class PerformanceMetrics(
  fboCreateCount: Long,
  fboReuseCount: Long,
  textureLoadCount: Long,
  textureReuseCount: Long,
  stateSwitchCount: Long,
  stateCacheHitCount: Long,
  viewportSetCount: Long,
  viewportCacheHitCount: Long,
  averageRenderTime: Double,
  averageFBOCreationTime: Double,
  averageTextureLoadTime: Double,
  memoryUsage: Long
)

/**
 * Real-time performance metrics
 */
case class RealtimeMetrics(
  currentFBOReuseRate: Double,
  currentTextureReuseRate: Double,
  currentStateCacheHitRate: Double,
  currentViewportCacheHitRate: Double,
  lastRenderTime: Long,
  lastMemoryUsage: Long
)
