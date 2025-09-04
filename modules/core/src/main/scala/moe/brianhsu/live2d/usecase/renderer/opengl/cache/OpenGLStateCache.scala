package moe.brianhsu.live2d.usecase.renderer.opengl.cache

import moe.brianhsu.live2d.enitiy.opengl.{OpenGLBinding, RichOpenGLBinding}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable

/**
 * OpenGL State Cache Manager
 * 
 * Optimization features:
 * 1. Cache OpenGL state query results
 * 2. Batch state save/restore
 * 3. State change detection, avoid duplicate settings
 */
class OpenGLStateCache(using gl: OpenGLBinding) {
  import gl.constants._

  // 状态缓存
  private val stateCache = new ConcurrentHashMap[Int, Any]()
  
  // 当前状态跟踪
  private val currentState = mutable.Map[Int, Any]()
  
  // 状态变化检测
  private var stateDirty = true

  /**
   * 保存当前OpenGL状态
   */
  def saveCurrentState(): OpenGLState = {
    val richGL = RichOpenGLBinding.wrapOpenGLBinding(gl)
    
    // 批量获取常用状态
    val states = Map(
      GL_TEXTURE_BINDING_2D -> richGL.openGLParameters(GL_TEXTURE_BINDING_2D),
      GL_FRAMEBUFFER_BINDING -> richGL.openGLParameters(GL_FRAMEBUFFER_BINDING),
      GL_VIEWPORT -> getViewport(),
      GL_BLEND -> getBooleanState(GL_BLEND),
      GL_DEPTH_TEST -> getBooleanState(GL_DEPTH_TEST),
      GL_CULL_FACE -> getBooleanState(GL_CULL_FACE),
      GL_SCISSOR_TEST -> getBooleanState(GL_SCISSOR_TEST)
    )
    
    OpenGLState(states)
  }

  /**
   * 恢复OpenGL状态
   */
  def restoreState(state: OpenGLState): Unit = {
    state.states.foreach { case (param, value) =>
      setStateIfChanged(param, value)
    }
    stateDirty = false
  }

  /**
   * 获取视口状态
   */
  private def getViewport(): (Int, Int, Int, Int) = {
    val viewport = new Array[Int](4)
    gl.glGetIntegerv(GL_VIEWPORT, viewport)
    (viewport(0), viewport(1), viewport(2), viewport(3))
  }

  /**
   * 获取布尔状态
   */
  private def getBooleanState(param: Int): Boolean = {
    val data = new Array[Byte](1)
    gl.glGetBooleanv(param, data)
    data(0) != 0
  }

  /**
   * 设置状态（仅在值改变时）
   */
  private def setStateIfChanged(param: Int, value: Any): Unit = {
    val currentValue = currentState.get(param)
    
    if (currentValue.isEmpty || currentValue.get != value) {
      value match {
        case intValue: Int =>
          param match {
            case GL_TEXTURE_BINDING_2D => gl.glBindTexture(GL_TEXTURE_2D, intValue)
            case GL_FRAMEBUFFER_BINDING => gl.glBindFramebuffer(GL_FRAMEBUFFER, intValue)
            case _ => // 其他整数参数的处理
          }
        case boolValue: Boolean =>
          param match {
            case GL_BLEND => if (boolValue) gl.glEnable(GL_BLEND) else gl.glDisable(GL_BLEND)
            case GL_DEPTH_TEST => if (boolValue) gl.glEnable(GL_DEPTH_TEST) else gl.glDisable(GL_DEPTH_TEST)
            case GL_CULL_FACE => if (boolValue) gl.glEnable(GL_CULL_FACE) else gl.glDisable(GL_CULL_FACE)
            case GL_SCISSOR_TEST => if (boolValue) gl.glEnable(GL_SCISSOR_TEST) else gl.glDisable(GL_SCISSOR_TEST)
            case _ => // 其他布尔参数的处理
          }
        case viewport: Product if viewport.productArity == 4 =>
          if (param == GL_VIEWPORT) {
            val x = viewport.productElement(0).asInstanceOf[Int]
            val y = viewport.productElement(1).asInstanceOf[Int]
            val width = viewport.productElement(2).asInstanceOf[Int]
            val height = viewport.productElement(3).asInstanceOf[Int]
            gl.glViewport(x, y, width, height)
          }
        case _ => // 其他类型的处理
      }
      
      currentState(param) = value
      stateDirty = true
    }
  }

  /**
   * 批量设置状态
   */
  def setStates(states: Map[Int, Any]): Unit = {
    states.foreach { case (param, value) =>
      setStateIfChanged(param, value)
    }
  }

  /**
   * 获取缓存大小
   */
  def getCacheSize: Int = stateCache.size()

  /**
   * 清理缓存
   */
  def clearCache(): Unit = {
    stateCache.clear()
    currentState.clear()
    stateDirty = true
  }

  /**
   * 检查状态是否已改变
   */
  def isStateDirty: Boolean = stateDirty

  /**
   * 标记状态为干净
   */
  def markStateClean(): Unit = {
    stateDirty = false
  }
}

/**
 * OpenGL状态快照
 */
case class OpenGLState(states: Map[Int, Any])(using gl: OpenGLBinding) {
  import gl.constants._
  
  /**
   * 获取特定参数的值
   */
  def get[T](param: Int): Option[T] = {
    states.get(param).map(_.asInstanceOf[T])
  }
  
  /**
   * 获取整数参数
   */
  def getInt(param: Int): Option[Int] = get[Int](param)
  
  /**
   * 获取布尔参数
   */
  def getBoolean(param: Int): Option[Boolean] = get[Boolean](param)
  
  /**
   * 获取视口
   */
  def getViewport: Option[(Int, Int, Int, Int)] = get[(Int, Int, Int, Int)](GL_VIEWPORT)
  
  /**
   * 合并状态
   */
  def merge(other: OpenGLState): OpenGLState = {
    OpenGLState(this.states ++ other.states)
  }
}
