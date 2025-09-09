package moe.brianhsu.live2d.demo.app

import moe.brianhsu.live2d.adapter.gateway.avatar.AvatarFileReader
import moe.brianhsu.live2d.adapter.gateway.core.JnaNativeCubismAPILoader
import moe.brianhsu.live2d.boundary.gateway.renderer.DrawCanvasInfoReader
import moe.brianhsu.live2d.demo.app.DemoApp.OnOpenGLThread
import moe.brianhsu.live2d.enitiy.avatar.Avatar
import moe.brianhsu.live2d.enitiy.model.Live2DModel
import moe.brianhsu.live2d.enitiy.opengl.OpenGLBinding
import moe.brianhsu.live2d.enitiy.updater.SystemNanoTimeBasedFrameInfo
import moe.brianhsu.live2d.usecase.renderer.opengl.AvatarRenderer
import moe.brianhsu.live2d.usecase.renderer.viewport.{ProjectionMatrixCalculator, ViewOrientation, ViewPortMatrixCalculator}
import moe.brianhsu.live2d.usecase.updater.impl.EasyUpdateStrategy
import moe.brianhsu.live2d.config.UnifiedConfig

import scala.annotation.unused
import scala.util.{Try, Success, Failure}
import moe.brianhsu.live2d.enitiy.model.parameter.StartupOptimizations.*

object DemoApp {
  type OnOpenGLThread = (=> Any) => Unit

  sealed trait FaceDirectionMode
  case object FollowMouse extends FaceDirectionMode
  case object ClickAndDrag extends FaceDirectionMode

  // 使用统一配置管理器的便捷方法
  def saveLastAvatar(path: String): Unit = UnifiedConfig.saveLastAvatar(path)

  def loadLastAvatarPath(): Option[String] = UnifiedConfig.loadLastAvatarPath()

  def saveAutoStart(enabled: Boolean): Unit = {
    val currentConfig = UnifiedConfig.config
    val newAutoStartSettings = currentConfig.autoStart.copy(enabled = enabled)
    UnifiedConfig.updateAutoStartSettings(newAutoStartSettings)
  }

  def loadAutoStart(): Boolean = UnifiedConfig.config.autoStart.enabled

  def saveEyeGaze(enabled: Boolean): Unit = {
    val currentConfig = UnifiedConfig.config
    val newAutoStartSettings = currentConfig.autoStart.copy(eyeGaze = enabled)
    UnifiedConfig.updateAutoStartSettings(newAutoStartSettings)
  }

  def loadEyeGaze(): Boolean = UnifiedConfig.config.autoStart.eyeGaze

  def savePupilGaze(enabled: Boolean): Unit = {
    val currentConfig = UnifiedConfig.config
    val newAutoStartSettings = currentConfig.autoStart.copy(pupilGaze = enabled)
    UnifiedConfig.updateAutoStartSettings(newAutoStartSettings)
  }

  def loadPupilGaze(): Boolean = UnifiedConfig.config.autoStart.pupilGaze

  def saveDisableEyeBlink(enabled: Boolean): Unit = {
    val currentConfig = UnifiedConfig.config
    val newAutoStartSettings = currentConfig.autoStart.copy(disableEyeBlink = enabled)
    UnifiedConfig.updateAutoStartSettings(newAutoStartSettings)
  }

  def loadDisableEyeBlink(): Boolean = UnifiedConfig.config.autoStart.disableEyeBlink
    
  def saveTransparentBackground(enabled: Boolean): Unit = {
    val currentConfig = UnifiedConfig.config
    val newAutoStartSettings = currentConfig.autoStart.copy(transparentBackground = enabled)
    UnifiedConfig.updateAutoStartSettings(newAutoStartSettings)
  }

  def loadTransparentBackground(): Boolean = UnifiedConfig.config.autoStart.transparentBackground

  def saveLanguage(language: String): Unit = {
    val currentConfig = UnifiedConfig.config
    val newLanguageSettings = currentConfig.language.copy(language = language)
    UnifiedConfig.updateLanguageSettings(newLanguageSettings)
  }

  def loadLanguage(): String = UnifiedConfig.config.language.language

  // Window settings management methods
  def saveWindowSettings(x: Int, y: Int, width: Int, height: Int, maximized: Boolean = false): Unit = {
    val newWindowSettings = UnifiedConfig.WindowSettings(x, y, width, height, maximized)
    UnifiedConfig.updateWindowSettings(newWindowSettings)
  }

  def loadWindowSettings(): Option[(Int, Int, Int, Int, Boolean)] = {
    val windowSettings = UnifiedConfig.config.window
    Some((windowSettings.x, windowSettings.y, windowSettings.width, windowSettings.height, windowSettings.maximized))
  }
}

abstract class DemoApp(drawCanvasInfo: DrawCanvasInfoReader, onOpenGLThread: OnOpenGLThread)
                      (protected override val openGL: OpenGLBinding) extends OpenGLBase(drawCanvasInfo, onOpenGLThread)(openGL) with SpriteControl with EffectControl {

  import openGL.constants._

  protected lazy val viewPortMatrixCalculator = new ViewPortMatrixCalculator
  protected lazy val projectionMatrixCalculator = new ProjectionMatrixCalculator(drawCanvasInfo)
  protected var mAvatarHolder: Option[Avatar] = None
  protected var modelHolder: Option[Live2DModel] = mAvatarHolder.map(_.model)
  protected var rendererHolder: Option[AvatarRenderer] = modelHolder.map(model => AvatarRenderer(model)(using openGL))
  protected var mUpdateStrategyHolder: Option[EasyUpdateStrategy] = None

  private given cubismCore: JnaNativeCubismAPILoader = new JnaNativeCubismAPILoader()
  private val frameTimeCalculator = new SystemNanoTimeBasedFrameInfo

  private var zoom: Float = 2.0f
  private var offsetX: Float = 0.0f
  private var offsetY: Float = 0.0f

  // Emoji shortcut mapping: e.g. '1' -> "Smile"
  private var expressionKeyMap: Map[Char, String] = Map()

  {
    // Startup optimization
    profileStartup("DemoApp Initialization") {
      optimizeSystemProperties()
      initOpenGL()
    }
  }

  def avatarHolder: Option[Avatar] = mAvatarHolder
  def strategyHolder: Option[EasyUpdateStrategy] = mUpdateStrategyHolder

  def resetModel(): Unit = {
    modelHolder.foreach(_.reset())
  }

  override def display(isForceUpdate: Boolean = false): Unit =
    clearScreen()

    sprites.foreach(spriteRenderer.draw)

    this.frameTimeCalculator.updateFrameTime()

    for
      avatar <- mAvatarHolder
      model <- modelHolder
      renderer <- rendererHolder
    do
      val projection = projectionMatrixCalculator.calculate(
        viewPortMatrixCalculator.viewPortMatrix,
        isForceUpdate,
        updateModelMatrix(model)
      )

      avatar.update(this.frameTimeCalculator)
      renderer.draw(projection)

  def updateModelMatrix(model: Live2DModel)(viewOrientation: ViewOrientation): Unit =
    model.modelMatrix = model.modelMatrix
      .scaleToHeight(zoom)
      .left(offsetX)
      .top(offsetY)

  def resize(): Unit = {
    viewPortMatrixCalculator.updateViewPort(
      drawCanvasInfo.currentCanvasWidth,
      drawCanvasInfo.currentCanvasHeight
    )

    openGL.glViewport(0, 0, drawCanvasInfo.currentSurfaceWidth, drawCanvasInfo.currentSurfaceHeight)

    sprites.foreach(_.resize())

    this.display()
  }

  override def onMouseReleased(x: Int, y: Int): Unit = {
    super.onMouseReleased(x, y)

    for {
      _ <- mAvatarHolder
      model <- modelHolder
    } {
      val transformedX = viewPortMatrixCalculator.drawCanvasToModelMatrix.transformedX(x.toFloat)
      val transformedY = viewPortMatrixCalculator.drawCanvasToModelMatrix.transformedY(y.toFloat)

      val hitAreaHolder = for {
        avatar <- mAvatarHolder
        hitArea <- avatar.avatarSettings.hitArea.find(area => model.isHit(area.id, transformedX, transformedY))
      } yield {
        hitArea.name
      }
      hitAreaHolder match {
        case Some(area) => onStatusUpdated(s"Clicked on Avatar's $area.")
        case None => onStatusUpdated("Clicked nothing.")
      }
    }
  }

  private def initOpenGL(): Unit = {
    viewPortMatrixCalculator.updateViewPort(
      drawCanvasInfo.currentCanvasWidth,
      drawCanvasInfo.currentCanvasHeight
    )

    openGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    openGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    openGL.glEnable(GL_BLEND)
    openGL.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
  }

  def startMotion(group: String, i: Int, isLoop: Boolean): Unit = {
    mUpdateStrategyHolder.foreach { updateStrategy =>
      updateStrategy.startMotion(group, i, isLoop)
    }
  }

  def startExpression(name: String): Unit = {
    mUpdateStrategyHolder.foreach { updateStrategy =>
      updateStrategy.startExpression(name)
    }
  }

  private def clearScreen(): Unit = {
    openGL.glClearColor(
      backgroundColor.getRed / 255.0f,
      backgroundColor.getGreen / 255.0f,
      backgroundColor.getBlue / 255.0f,
      backgroundColor.getAlpha / 255.0f
    )
    openGL.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
    openGL.glClearDepth(1.0)
  }

  def switchAvatar(directoryPath: String): Try[Avatar] = {
    profileStartup(s"Avatar Loading: $directoryPath") {
      onStatusUpdated(s"Loading $directoryPath...")

      this.disableMicLipSync()
      this.enableLipSyncFromMotionSound(false)

      val newAvatarHolder = new AvatarFileReader(directoryPath).loadAvatar()

      newAvatarHolder match
        case Success(avatar) =>
          this.mAvatarHolder = Some(avatar)
          this.modelHolder = Some(avatar.model)
          this.mUpdateStrategyHolder = Some(new EasyUpdateStrategy(avatar, faceDirectionCalculator))
          
          avatar.updateStrategyHolder = this.mUpdateStrategyHolder
          onStatusUpdated(s"$directoryPath loaded successfully.")
          avatar.model.parameters.keySet.foreach(println)

          // Emoji Shortcut Binding Update - Optimized with view for better performance
          this.expressionKeyMap = avatar.avatarSettings.expressions.view
            .take(9)
            .zipWithIndex
            .map { case ((expressionName, _), i) => ((i + '1').toChar, expressionName) }
            .toMap

          // Console outputs current mapping for debugging 
          println("Expression shortcut mapping created:") 
          expressionKeyMap.foreach { case (k, v) => println(s" key '$k' -> expression '$v'") }

          onOpenGLThread {
            this.rendererHolder = Some(AvatarRenderer(avatar.model)(using openGL))
            initOpenGL()
            // Render the first frame after the OpenGL context is initialized
            display()
          }

          DemoApp.saveLastAvatar(directoryPath)
          onAvatarLoaded(this)
          
        case Failure(e) =>
          onStatusUpdated(s"Failed to load $directoryPath: ${e.getMessage}")
          println(s"Avatar loading failed: ${e.getMessage}")
          e.printStackTrace()

      newAvatarHolder
    }
  }

  def move(offsetX: Float, offsetY: Float): Unit = {
    this.offsetX += offsetX
    this.offsetY += offsetY
    this.display(true)
  }

  def zoom(level: Float): Unit = {
    this.zoom = (this.zoom + level).max(0.5f)
    this.display(true)
  }

  def keyReleased(key: Char): Unit = {
    key match {
      case 'z' => switchAvatar("src/main/resources/Haru")
      case 'x' => switchAvatar("src/main/resources/Mark")
      case 'c' => switchAvatar("src/main/resources/Rice")
      case 'v' => switchAvatar("src/main/resources/Natori")
      case 'b' => switchAvatar("src/main/resources/Hiyori")
              case 'r' => switchAvatar("runtime")  // Add runtime model loading
      case _ =>
        //  Check if it is a numeric key to perform expression switching
        expressionKeyMap.get(key).foreach { expressionName =>
          println(s"Emoji shortcut：$expressionName")
          startExpression(expressionName)
        }
    }
  }

  def onAvatarLoaded(live2DView: DemoApp): Unit = {}
  def onStatusUpdated(status: String): Unit = {}
}
