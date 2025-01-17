package moe.brianhsu.live2d.adapter.gateway.model

import com.sun.jna.Memory
import moe.brianhsu.live2d.adapter.gateway.core.JnaNativeCubismAPILoader
import moe.brianhsu.live2d.boundary.gateway.core.NativeCubismAPILoader
import moe.brianhsu.live2d.boundary.gateway.core.memory.MemoryAllocator
import moe.brianhsu.live2d.enitiy.core.NativeCubismAPI
import moe.brianhsu.live2d.enitiy.core.memory.MemoryInfo
import moe.brianhsu.live2d.enitiy.core.types.{MocAlignment, MocVersion42}
import moe.brianhsu.live2d.enitiy.model.MocInfo
import moe.brianhsu.live2d.exception.MocInconsistentException
import org.scalamock.scalatest.MockFactory
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{GivenWhenThen, TryValues}

import java.nio.file.{Files, NoSuchFileException, Paths}

class MocInfoFileReaderFeature extends AnyFeatureSpec with GivenWhenThen with Matchers with MockFactory with TryValues {

  Feature("Read .moc3 from file") {

    Scenario("Read exist and correct .moc3 file into allocated mocInfo memory") {
      Given("a exist .moc3 file")
      val modelFile = "src/test/resources/models/Mao/Mao.moc3"
      val fileContent = Files.readAllBytes(Paths.get(modelFile))

      And("a set of mocked memory to be allocated and written")
      val mockedOriginalMemory = mock[MockableMemory]
      val mockedAlignedMemory = mock[MockableMemory]
      val mockedMemoryInfo = MemoryInfo(mockedOriginalMemory, mockedAlignedMemory)
      val mockedMemoryAllocator = mock[MemoryAllocator]
      implicit val core: NativeCubismAPILoader = createMockedCubismCore(mockedMemoryAllocator)

      (mockedMemoryAllocator.allocate _).expects(fileContent.size, MocAlignment).returning(mockedMemoryInfo).once()
      (mockedAlignedMemory.write: (Long, Array[Byte], Int, Int) => Unit).expects(0, *, 0, fileContent.size).once()

      When("read .moc file using MocInfoFileReader")
      val mocInfoFileReader = new MocInfoFileReader(modelFile, shouldCheckConsistent = false)

      Then("it should be a success")
      val mocInfo = mocInfoFileReader.loadMocInfo().success.value

      And("it should return correct information")
      mocInfo.memory shouldBe mockedMemoryInfo
      mocInfo.originalSize shouldBe fileContent.size
    }

    Scenario("Fetch mocVersion from MocInfo") {
      Given("a exist .moc3 file")
      val modelFile = "src/test/resources/models/Mao/Mao.moc3"

      When("read .moc file using MocInfoFileReader")
      implicit val core: NativeCubismAPILoader = new JnaNativeCubismAPILoader()
      val mocInfoFileReader = new MocInfoFileReader(modelFile)

      Then("it should be a success")
      val mocInfo = mocInfoFileReader.loadMocInfo().success.value

      And("it should return correct mocVersion")
      mocInfo.mocVersion shouldBe MocVersion42
    }

    Scenario("Read non-exist .mocFile into mocInfo") {
      Given("a set of mocked memory")
      val mockedMemoryAllocator = mock[MemoryAllocator]
      implicit val core: NativeCubismAPILoader = createMockedCubismCore(mockedMemoryAllocator)

      When("read .moc file using MocInfoFileReader")
      val mocInfoFileReader = new MocInfoFileReader("nonExistFile")

      Then("it should be a Failure[NoSuchFileException]")
      val exception = mocInfoFileReader.loadMocInfo().failure.exception
      exception shouldBe a[NoSuchFileException]
    }

  }

  Feature("Check consistency of a .moc file") {
    Scenario("Enable consistent check and .moc is inconsistent") {
      Given("a corrupted .moc3 file")
      val modelFile = "src/test/resources/models/corruptedModel/corruptedMoc3/haru_greeter_t03.moc3"

      When("read .moc file using MocInfoFileReader and enable consistent check")
      implicit val core: NativeCubismAPILoader = new JnaNativeCubismAPILoader()
      val mocInfoFileReader = new MocInfoFileReader(modelFile, shouldCheckConsistent = true)

      Then("it should be a Failure[MocInconsistentException]")
      val exception = mocInfoFileReader.loadMocInfo().failure.exception
      exception shouldBe a[MocInconsistentException]
    }

    Scenario("Enable consistent check and .moc is consistent") {
      Given("a exist .moc3 file")
      val modelFile = "src/test/resources/models/Mao/Mao.moc3"

      When("read .moc file using MocInfoFileReader and enable consistent check")
      implicit val core: NativeCubismAPILoader = new JnaNativeCubismAPILoader()
      val mocInfoFileReader = new MocInfoFileReader(modelFile, shouldCheckConsistent = true)

      Then("it should be a Success[MocInfo]")
      val mocInfo = mocInfoFileReader.loadMocInfo().success.value
      mocInfo shouldBe a[MocInfo]
    }

    Scenario("Disable consistent check and .moc is inconsistent") {
      Given("a corrupted .moc3 file")
      val modelFile = "src/test/resources/models/corruptedModel/corruptedMoc3/haru_greeter_t03.moc3"

      When("read .moc file using MocInfoFileReader and disable consistent check")
      implicit val core: NativeCubismAPILoader = new JnaNativeCubismAPILoader()
      val mocInfoFileReader = new MocInfoFileReader(modelFile, shouldCheckConsistent = false)

      Then("it should be a Success[MocInfo]")
      val mocInfo = mocInfoFileReader.loadMocInfo().success.value
      mocInfo shouldBe a[MocInfo]
    }

    Scenario("Disable consistent check and .moc is consistent") {
      Given("a exist .moc3 file")
      val modelFile = "src/test/resources/models/Mao/Mao.moc3"

      When("read .moc file using MocInfoFileReader and enable consistent check")
      implicit val core: NativeCubismAPILoader = new JnaNativeCubismAPILoader()
      val mocInfoFileReader = new MocInfoFileReader(modelFile, shouldCheckConsistent = false)

      Then("it should be a Success[MocInfo]")
      val mocInfo = mocInfoFileReader.loadMocInfo().success.value
      mocInfo shouldBe a[MocInfo]
    }
  }

  private def createMockedCubismCore(mockedMemoryAllocator: MemoryAllocator) = {
    new NativeCubismAPILoader {
      override implicit val memoryAllocator: MemoryAllocator = mockedMemoryAllocator
      override val cubismAPI: NativeCubismAPI = stub[NativeCubismAPI]
    }
  }

  class MockableMemory extends Memory(1024)

}
