package moe.brianhsu.live2d.adapter.gateway.renderer.jogl

import com.jogamp.opengl.awt.GLCanvas
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

import java.awt.GraphicsConfiguration
import java.awt.geom.AffineTransform

class JOGLCanvasInfoFeature extends AnyFeatureSpec with Matchers with GivenWhenThen {
  Feature("Get canvas information") {
    Scenario("Get canvas information from Java OpenGL AWT Canvas") {
      Given("a test Java OpenGL AWT Canvas")
      val testConfiguration = new GraphicsConfiguration {
        override def getDefaultTransform: AffineTransform = {
          val transform = new AffineTransform()
          transform.setToScale(1.5, 2.5)
          transform
        }
        override def getNormalizingTransform: AffineTransform = new AffineTransform()
        override def getBounds: java.awt.Rectangle = new java.awt.Rectangle(0, 0, 100, 100)
        override def getColorModel: java.awt.image.ColorModel = null
        override def getColorModel(transparency: Int): java.awt.image.ColorModel = null
        override def getImageCapabilities: java.awt.ImageCapabilities = null
        override def getBufferCapabilities: java.awt.BufferCapabilities = null
        override def createCompatibleVolatileImage(width: Int, height: Int): java.awt.image.VolatileImage = null
        override def createCompatibleVolatileImage(width: Int, height: Int, caps: java.awt.ImageCapabilities): java.awt.image.VolatileImage = null
        override def getDevice: java.awt.GraphicsDevice = null
      }

      val canvas = new GLCanvas {
        override def getWidth: Int = 123
        override def getHeight: Int = 456
        override def getGraphicsConfiguration: GraphicsConfiguration = testConfiguration
      }

      And("a JOGLCanvasInfo based on that canvas")
      val canvasInfo = new JOGLCanvasInfoReader(canvas)

      Then("it should have correct properties")
      canvasInfo.currentCanvasWidth shouldBe 123
      canvasInfo.currentCanvasHeight shouldBe 456
      canvasInfo.currentSurfaceWidth shouldBe 184
      canvasInfo.currentSurfaceHeight shouldBe 1140
    }
  }

}
