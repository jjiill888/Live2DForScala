package moe.brianhsu.live2d.demo.app

import moe.brianhsu.live2d.demo.sprite.BackgroundSprite
import moe.brianhsu.live2d.enitiy.opengl.sprite.Sprite
import moe.brianhsu.live2d.enitiy.opengl.texture.TextureInfo
import moe.brianhsu.live2d.usecase.renderer.opengl.shader.SpriteShader
import moe.brianhsu.live2d.usecase.renderer.opengl.sprite.SpriteRenderer

import java.awt.Color
import scala.util.Try

trait SpriteControl {
  this: OpenGLBase =>

  protected var backgroundColor = new Color(0, 255, 0)

  protected var sprites: List[Sprite] = Nil

  protected val spriteRenderer = new SpriteRenderer(new SpriteShader(using openGL))(using openGL)

  def switchToDefaultBackground(): Unit = {
    switchToPureColorBackground(new Color(0, 255, 0))
  }

  def changeBackground(filePath: String): Try[Unit] = Try {
    onOpenGLThread {
      this.sprites =
        createBackgroundSprite(filePath) ::
          this.sprites.filterNot(_.isInstanceOf[BackgroundSprite])
    }
  }

  def switchToPureColorBackground(color: Color): Unit = {
    this.backgroundColor = color
    this.sprites = this.sprites.filterNot(_.isInstanceOf[BackgroundSprite])
    this.display(true)
  }
  
  def setTransparentBackground(enabled: Boolean): Unit =
    if (enabled) switchToTransparentBackground() else switchToDefaultBackground()

  def switchToTransparentBackground(): Unit = {
    this.backgroundColor = new Color(0, 0, 0, 0)
    this.sprites = this.sprites.filterNot(_.isInstanceOf[BackgroundSprite])
    this.display(true)
  }

  protected def createBackgroundSprite(textureFile: String): BackgroundSprite = {
    new BackgroundSprite(
      drawCanvasInfo,
      textureManager.loadTexture(textureFile)
    )
  }

}
