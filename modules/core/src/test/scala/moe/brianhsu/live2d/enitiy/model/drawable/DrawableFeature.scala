package moe.brianhsu.live2d.enitiy.model.drawable

import com.sun.jna.Pointer
import moe.brianhsu.live2d.enitiy.model.drawable.Drawable.ColorFetcher
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class DrawableFeature extends AnyFeatureSpec with GivenWhenThen with Matchers
  with MockFactory with TableDrivenPropertyChecks {

  private val mockedFetcher: ColorFetcher = () => DrawableColor(1.0f, 1.0f, 1.0f, 1.0f)

  class MockablePointer extends Pointer(0)

  Feature("Get draw order") {
    Scenario("get draw order value from pointer") {
      Given("a drawable with a mocked pointer to draw order value")
      val drawOrderPointer = stub[MockablePointer]
      val drawable = Drawable("drawableId", index = 0, None, ConstantFlags(0), new DynamicFlags(stub[MockablePointer]), textureIndex = 0, masks = Nil, stub[VertexInfo], drawOrderPointer, renderOrderPointer = null, opacityPointer = null, mockedFetcher, mockedFetcher)

      And("the mocked pointer return integer value 123 when request with offset=0")
      (drawOrderPointer.getInt _).when(0).returning(123)

      When("ask drawOrder from drawable")
      val drawOrder = drawable.drawOrder

      Then("it should be 123")
      drawOrder shouldBe 123
    }
  }

  Feature("Get render order") {
    Scenario("get render order value from pointer") {
      Given("a drawable with a mocked pointer to render order value")
      val renderOrderPointer = stub[MockablePointer]
      val drawable = Drawable("drawableId", index = 0, None, ConstantFlags(0), new DynamicFlags(stub[MockablePointer]), textureIndex = 0, masks = Nil, stub[VertexInfo], drawOrderPointer = null, renderOrderPointer, opacityPointer = null, mockedFetcher, mockedFetcher)

      And("the mocked pointer return integer value 456 when request with offset=0")
      (renderOrderPointer.getInt _).when(0).returning(456)

      When("ask renderOrder from drawable")
      val renderOrder = drawable.renderOrder

      Then("it should be 456")
      renderOrder shouldBe 456
    }
  }

  Feature("Get opacity") {
    Scenario("Get opacity value from pointer") {
      Given("a drawable with a mocked pointer to opacity value")
      val opacityPointer = stub[MockablePointer]
      val drawable = Drawable("drawableId", index = 0, None, ConstantFlags(0), new DynamicFlags(stub[MockablePointer]), textureIndex = 0, masks = Nil, stub[VertexInfo], drawOrderPointer = null, renderOrderPointer = null, opacityPointer, mockedFetcher, mockedFetcher)

      And("the mocked pointer return integer value 0.65 when request with offset=0")
      (opacityPointer.getFloat _).when(0).returning(0.65f)

      When("ask opacity from drawable")
      val opacity = drawable.opacity

      Then("it should be 0.65")
      opacity shouldBe 0.65f
    }
  }

  Feature("Drawable isCulling") {
    Scenario("Decide isCulling from ConstantFlag") {
      info("It will be a inverted boolean for doubleSided bit flag in constant flag")
      info("The doubleSided flag is at third least significant bit")

      val dataTable = Table(
        ("flagInBinary", "expectedResult"),
        ("00000000",                  true),
        ("01111111",                 false),
        ("00000100",                 false),
        ("01111011",                  true),
      )

      forAll(dataTable) { (flagsInBinary, expectedResult) =>

        Given(s"a drawable with constantFlag with bitValue=$flagsInBinary")
        val flagByte = java.lang.Byte.parseByte(flagsInBinary, 2)
        val drawable = Drawable("drawableId", index = 0, None, ConstantFlags(flagByte), new DynamicFlags(stub[MockablePointer]), textureIndex = 0, masks = Nil, stub[VertexInfo], drawOrderPointer = null, renderOrderPointer = null, opacityPointer = null, mockedFetcher, mockedFetcher)

        When("request for isCulling attribute")
        val isCulling = drawable.isCulling

        Then(s"it should be $expectedResult")
        isCulling shouldBe expectedResult
      }

    }
  }

}
