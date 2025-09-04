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
  
  // 性能计数器
  private val fboCreateCount = new AtomicLong(0)
  private val fboReuseCount = new AtomicLong(0)
  private val textureLoadCount = new AtomicLong(0)
  private val textureReuseCount = new AtomicLong(0)
  private val stateSwitchCount = new AtomicLong(0)
  private val stateCacheHitCount = new AtomicLong(0)
  private val viewportSetCount = new AtomicLong(0)
  private val viewportCacheHitCount = new AtomicLong(0)
  
  // 时间统计
  private val renderTimes = mutable.ArrayBuffer[Long]()
  private val fboCreateTimes = mutable.ArrayBuffer[Long]()
  private val textureLoadTimes = mutable.ArrayBuffer[Long]()
  
  // 内存使用统计
  private val memoryUsage = mutable.ArrayBuffer[Long]()
  
  // 开始时间记录
  private var renderStartTime: Long = 0
  private var fboCreateStartTime: Long = 0
  private var textureLoadStartTime: Long = 0

  /**
   * 开始渲染计时
   */
  def startRenderTiming(): Unit = {
    renderStartTime = System.nanoTime()
  }

  /**
   * 结束渲染计时
   */
  def endRenderTiming(): Unit = {
    val duration = System.nanoTime() - renderStartTime
    renderTimes += duration
  }

  /**
   * 记录FBO创建
   */
  def recordFBOCreation(isReuse: Boolean): Unit = {
    if (isReuse) {
      fboReuseCount.incrementAndGet()
    } else {
      fboCreateCount.incrementAndGet()
    }
  }

  /**
   * 开始FBO创建计时
   */
  def startFBOCreationTiming(): Unit = {
    fboCreateStartTime = System.nanoTime()
  }

  /**
   * 结束FBO创建计时
   */
  def endFBOCreationTiming(): Unit = {
    val duration = System.nanoTime() - fboCreateStartTime
    fboCreateTimes += duration
  }

  /**
   * 记录纹理加载
   */
  def recordTextureLoad(isReuse: Boolean): Unit = {
    if (isReuse) {
      textureReuseCount.incrementAndGet()
    } else {
      textureLoadCount.incrementAndGet()
    }
  }

  /**
   * 开始纹理加载计时
   */
  def startTextureLoadTiming(): Unit = {
    textureLoadStartTime = System.nanoTime()
  }

  /**
   * 结束纹理加载计时
   */
  def endTextureLoadTiming(): Unit = {
    val duration = System.nanoTime() - textureLoadStartTime
    textureLoadTimes += duration
  }

  /**
   * 记录状态切换
   */
  def recordStateSwitch(isCacheHit: Boolean): Unit = {
    stateSwitchCount.incrementAndGet()
    if (isCacheHit) {
      stateCacheHitCount.incrementAndGet()
    }
  }

  /**
   * 记录视口设置
   */
  def recordViewportSet(isCacheHit: Boolean): Unit = {
    viewportSetCount.incrementAndGet()
    if (isCacheHit) {
      viewportCacheHitCount.incrementAndGet()
    }
  }

  /**
   * 记录内存使用
   */
  def recordMemoryUsage(bytes: Long): Unit = {
    memoryUsage += bytes
  }

  /**
   * 获取性能统计报告
   */
  def getPerformanceReport: PerformanceReport = {
    val totalFBOOps = fboCreateCount.get() + fboReuseCount.get()
    val totalTextureOps = textureLoadCount.get() + textureReuseCount.get()
    val totalStateOps = stateSwitchCount.get()
    val totalViewportOps = viewportSetCount.get()
    
    PerformanceReport(
      // FBO统计
      fboCreateCount = fboCreateCount.get(),
      fboReuseCount = fboReuseCount.get(),
      fboReuseRate = if (totalFBOOps > 0) fboReuseCount.get().toDouble / totalFBOOps else 0.0,
      avgFBOCreationTime = if (fboCreateTimes.nonEmpty) fboCreateTimes.sum / fboCreateTimes.length else 0L,
      
      // 纹理统计
      textureLoadCount = textureLoadCount.get(),
      textureReuseCount = textureReuseCount.get(),
      textureReuseRate = if (totalTextureOps > 0) textureReuseCount.get().toDouble / totalTextureOps else 0.0,
      avgTextureLoadTime = if (textureLoadTimes.nonEmpty) textureLoadTimes.sum / textureLoadTimes.length else 0L,
      
      // 状态统计
      stateSwitchCount = stateSwitchCount.get(),
      stateCacheHitCount = stateCacheHitCount.get(),
      stateCacheHitRate = if (totalStateOps > 0) stateCacheHitCount.get().toDouble / totalStateOps else 0.0,
      
      // 视口统计
      viewportSetCount = viewportSetCount.get(),
      viewportCacheHitCount = viewportCacheHitCount.get(),
      viewportCacheHitRate = if (totalViewportOps > 0) viewportCacheHitCount.get().toDouble / totalViewportOps else 0.0,
      
      // 渲染统计
      avgRenderTime = if (renderTimes.nonEmpty) renderTimes.sum / renderTimes.length else 0L,
      minRenderTime = if (renderTimes.nonEmpty) renderTimes.min else 0L,
      maxRenderTime = if (renderTimes.nonEmpty) renderTimes.max else 0L,
      
      // 内存统计
      avgMemoryUsage = if (memoryUsage.nonEmpty) memoryUsage.sum / memoryUsage.length else 0L,
      maxMemoryUsage = if (memoryUsage.nonEmpty) memoryUsage.max else 0L,
      
      // 总体统计
      totalOperations = totalFBOOps + totalTextureOps + totalStateOps + totalViewportOps,
      totalCacheHits = fboReuseCount.get() + textureReuseCount.get() + stateCacheHitCount.get() + viewportCacheHitCount.get(),
      overallCacheHitRate = {
        val totalOps = totalFBOOps + totalTextureOps + totalStateOps + totalViewportOps
        if (totalOps > 0) (fboReuseCount.get() + textureReuseCount.get() + stateCacheHitCount.get() + viewportCacheHitCount.get()).toDouble / totalOps else 0.0
      }
    )
  }

  /**
   * 重置所有统计
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
   * 获取实时性能指标
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
 * 性能报告
 */
case class PerformanceReport(
  // FBO统计
  fboCreateCount: Long,
  fboReuseCount: Long,
  fboReuseRate: Double,
  avgFBOCreationTime: Long,
  
  // 纹理统计
  textureLoadCount: Long,
  textureReuseCount: Long,
  textureReuseRate: Double,
  avgTextureLoadTime: Long,
  
  // 状态统计
  stateSwitchCount: Long,
  stateCacheHitCount: Long,
  stateCacheHitRate: Double,
  
  // 视口统计
  viewportSetCount: Long,
  viewportCacheHitCount: Long,
  viewportCacheHitRate: Double,
  
  // 渲染统计
  avgRenderTime: Long,
  minRenderTime: Long,
  maxRenderTime: Long,
  
  // 内存统计
  avgMemoryUsage: Long,
  maxMemoryUsage: Long,
  
  // 总体统计
  totalOperations: Long,
  totalCacheHits: Long,
  overallCacheHitRate: Double
) {
  
  /**
   * 格式化报告为字符串
   */
  def formatReport: String = {
    s"""
    |=== 离屏渲染性能报告 ===
    |FBO统计:
    |  创建次数: $fboCreateCount
    |  复用次数: $fboReuseCount
    |  复用率: ${String.format("%.2f%%", fboReuseRate * 100)}
    |  平均创建时间: ${avgFBOCreationTime / 1000000.0}ms
    |
    |纹理统计:
    |  加载次数: $textureLoadCount
    |  复用次数: $textureReuseCount
    |  复用率: ${String.format("%.2f%%", textureReuseRate * 100)}
    |  平均加载时间: ${avgTextureLoadTime / 1000000.0}ms
    |
    |状态统计:
    |  切换次数: $stateSwitchCount
    |  缓存命中: $stateCacheHitCount
    |  缓存命中率: ${String.format("%.2f%%", stateCacheHitRate * 100)}
    |
    |视口统计:
    |  设置次数: $viewportSetCount
    |  缓存命中: $viewportCacheHitCount
    |  缓存命中率: ${String.format("%.2f%%", viewportCacheHitRate * 100)}
    |
    |渲染统计:
    |  平均渲染时间: ${avgRenderTime / 1000000.0}ms
    |  最小渲染时间: ${minRenderTime / 1000000.0}ms
    |  最大渲染时间: ${maxRenderTime / 1000000.0}ms
    |
    |内存统计:
    |  平均内存使用: ${avgMemoryUsage / 1024.0 / 1024.0}MB
    |  最大内存使用: ${maxMemoryUsage / 1024.0 / 1024.0}MB
    |
    |总体统计:
    |  总操作数: $totalOperations
    |  总缓存命中: $totalCacheHits
    |  总体缓存命中率: ${String.format("%.2f%%", overallCacheHitRate * 100)}
    |=======================
    """.stripMargin
  }
}

/**
 * 实时性能指标
 */
case class RealtimeMetrics(
  currentFBOReuseRate: Double,
  currentTextureReuseRate: Double,
  currentStateCacheHitRate: Double,
  currentViewportCacheHitRate: Double,
  lastRenderTime: Long,
  lastMemoryUsage: Long
)
