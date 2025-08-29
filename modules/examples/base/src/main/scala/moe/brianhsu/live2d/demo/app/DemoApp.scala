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

import scala.annotation.unused
import scala.util.Try
import java.io.{File, PrintWriter}
import scala.io.Source

object DemoApp {
  type OnOpenGLThread = (=> Any) => Unit

  sealed trait FaceDirectionMode
  case object FollowMouse extends FaceDirectionMode
  case object ClickAndDrag extends FaceDirectionMode

  private val LastAvatarFile = new File("last_avatar")
  private val AutoStartFile = new File("auto_start.txt")

  private def readSettings(): Map[String, String] =
    if (AutoStartFile.exists()) {
      try {
        val src = Source.fromFile(AutoStartFile, "UTF-8")
        try {
          val lines = src.getLines().toList.map(_.trim).filter(_.nonEmpty)
          if (lines.size == 1 && !lines.head.contains("=")) {
            Map("autoStart" -> lines.head)
          } else {
            lines.flatMap { line =>
              line.split("=", 2) match {
                case Array(k, v) => Some(k -> v)
                case _ => None
              }
            }.toMap
          }
        } finally src.close()
      } catch {
        case e: Exception =>
          System.err.println(s"[WARN] Cannot read settings: ${e.getMessage}")
          Map.empty
      }
    } else Map.empty

  private def writeSettings(settings: Map[String, String]): Unit =
    try {
      val writer = new PrintWriter(AutoStartFile, "UTF-8")
      try settings.foreach { case (k, v) => writer.println(s"$k=$v") } finally writer.close()
    } catch {
      case e: Exception =>
        System.err.println(s"[WARN] Cannot save settings: ${e.getMessage}")
    }

  def saveLastAvatar(path: String): Unit =
    try {
      val writer = new PrintWriter(LastAvatarFile, "UTF-8")
      try writer.print(path) finally writer.close()
    } catch {
      case e: Exception =>
        System.err.println(s"[WARN] Cannot save last avatar path: ${e.getMessage}")
    }

  def loadLastAvatarPath(): Option[String] =
    if (LastAvatarFile.exists()) {
      try {
        val src = Source.fromFile(LastAvatarFile, "UTF-8")
        try Some(src.mkString.trim) finally src.close()
      } catch {
        case e: Exception =>
          System.err.println(s"[WARN] Cannot read last avatar path: ${e.getMessage}")
          None
      }
    } else None

  def saveAutoStart(enabled: Boolean): Unit = {
    val settings = readSettings() + ("autoStart" -> enabled.toString)
    writeSettings(settings)
  }

  def loadAutoStart(): Boolean =
    readSettings().get("autoStart").flatMap(_.toBooleanOption).getOrElse(false)

  def saveEyeGaze(enabled: Boolean): Unit = {
    val settings = readSettings() + ("eyeGaze" -> enabled.toString)
    writeSettings(settings)
  }

  def loadEyeGaze(): Boolean =
    readSettings().get("eyeGaze").flatMap(_.toBooleanOption).getOrElse(false)

  def savePupilGaze(enabled: Boolean): Unit = {
    val settings = readSettings() + ("pupilGaze" -> enabled.toString)
    writeSettings(settings)
  }

  def loadPupilGaze(): Boolean =
    readSettings().get("pupilGaze").flatMap(_.toBooleanOption).getOrElse(true)


  def saveDisableEyeBlink(enabled: Boolean): Unit = {
    val settings = readSettings() + ("disableEyeBlink" -> enabled.toString)
    writeSettings(settings)
  }

  def loadDisableEyeBlink(): Boolean =
    readSettings().get("disableEyeBlink").flatMap(_.toBooleanOption).getOrElse(false)
    
  def saveTransparentBackground(enabled: Boolean): Unit = {
    val settings = readSettings() + ("transparentBackground" -> enabled.toString)
    writeSettings(settings)
  }

  def loadTransparentBackground(): Boolean =
    readSettings().get("transparentBackground").flatMap(_.toBooleanOption).getOrElse(false)
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

  // Emoji shortcut mapping: e.g. ‘1’ -> “Smile”
  private var expressionKeyMap: Map[Char, String] = Map()

  {
    initOpenGL()
  }

  def avatarHolder: Option[Avatar] = mAvatarHolder
  def strategyHolder: Option[EasyUpdateStrategy] = mUpdateStrategyHolder

  def resetModel(): Unit = {
    modelHolder.foreach(_.reset())
  }

  override def display(isForceUpdate: Boolean = false): Unit = {
    clearScreen()

    sprites.foreach(spriteRenderer.draw)

    this.frameTimeCalculator.updateFrameTime()

    for {
      avatar <- mAvatarHolder
      model <- modelHolder
      renderer <- rendererHolder
    } {

      val projection = projectionMatrixCalculator.calculate(
        viewPortMatrixCalculator.viewPortMatrix,
        isForceUpdate,
        updateModelMatrix(model)
      )

      avatar.update(this.frameTimeCalculator)
      renderer.draw(projection)
    }

    def updateModelMatrix(model: Live2DModel)(viewOrientation: ViewOrientation): Unit = {
      model.modelMatrix = model.modelMatrix
        .scaleToHeight(zoom)
        .left(offsetX)
        .top(offsetY)
    }
  }

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
    onStatusUpdated(s"Loading $directoryPath...")

    this.disableMicLipSync()
    this.enableLipSyncFromMotionSound(false)

    val newAvatarHolder = new AvatarFileReader(directoryPath).loadAvatar()

    this.mAvatarHolder = newAvatarHolder.toOption.orElse(this.mAvatarHolder)
    this.modelHolder = mAvatarHolder.map(_.model)
    this.mUpdateStrategyHolder = mAvatarHolder.map(avatar => new EasyUpdateStrategy(avatar, faceDirectionCalculator))
    this.mAvatarHolder.foreach { avatar =>
      avatar.updateStrategyHolder = this.mUpdateStrategyHolder
      onStatusUpdated(s"$directoryPath loaded.")
      avatar.model.parameters.keySet.foreach(println)

      // Emoji Shortcut Binding Update
      this.expressionKeyMap = avatar.avatarSettings.expressions.toSeq.zipWithIndex
      .take(9)
      .map { case ((expressionName, _), i) => ((i + '1').toChar, expressionName) }
      .toMap


      // Console outputs current mapping for debugging 
      println("Expression shortcut mapping created:") 
      expressionKeyMap.foreach { case (k, v) => println(s" key '$k' -> expression '$v'") } 
 }

    onOpenGLThread {
      this.rendererHolder = modelHolder.map(model => AvatarRenderer(model)(using openGL))
      initOpenGL()
            // Render the first frame after the OpenGL context is initialized
      display()
    }

     newAvatarHolder.foreach { _ =>
      DemoApp.saveLastAvatar(directoryPath)
      onAvatarLoaded(this)
    }
    newAvatarHolder
  }

  def move(offsetX: Float, offsetY: Float): Unit = {
    this.offsetX += offsetX
    this.offsetY += offsetY
    this.display(true)
  }

  def zoom(level: Float): Unit = {
    this.zoom = (level + zoom).max(0.5f)
    this.display(true)
  }

  def keyReleased(key: Char): Unit = {
    key match {
      case 'z' => switchAvatar("src/main/resources/Haru")
      case 'x' => switchAvatar("src/main/resources/Mark")
      case 'c' => switchAvatar("src/main/resources/Rice")
      case 'v' => switchAvatar("src/main/resources/Natori")
      case 'b' => switchAvatar("src/main/resources/Hiyori")
      case _ =>
        //  Check if it is a numeric key to perform expression switching
        expressionKeyMap.get(key).foreach { expressionName =>
          println(s"Emoji shortcut：$expressionName")
          startExpression(expressionName)
        }
    }
  }

  def onAvatarLoaded(live2DView: DemoApp): Unit
  def onStatusUpdated(status: String): Unit
}
