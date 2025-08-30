package moe.brianhsu.live2d.adapter.gateway.avatar.settings.json

import moe.brianhsu.live2d.adapter.RichPath._
import moe.brianhsu.live2d.enitiy.avatar.settings.Settings
import moe.brianhsu.live2d.enitiy.avatar.settings.detail.{ExpressionSetting, MotionSetting, PhysicsSetting, PoseSetting, HitAreaSetting}
import moe.brianhsu.live2d.adapter.gateway.avatar.settings.json.model.{Group, ModelSetting, FileReferences, MotionFile}
import moe.brianhsu.live2d.boundary.gateway.avatar.SettingsReader
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats, JArray}
import org.json4s.MonadicJValue.jvalueToMonadic
import org.json4s.jvalue2extractable
import scala.reflect.ClassTag

// ClassTag to Manifest bridge for json4s compatibility
given [T](using ct: ClassTag[T]): scala.reflect.Manifest[T] = 
  new scala.reflect.Manifest[T] {
    override def runtimeClass: Class[_] = ct.runtimeClass
    override def typeArguments: List[scala.reflect.Manifest[_]] = Nil
    override def arrayManifest: scala.reflect.Manifest[Array[T]] = 
      new scala.reflect.Manifest[Array[T]] {
        override def runtimeClass: Class[_] = java.lang.reflect.Array.newInstance(ct.runtimeClass, 0).getClass
        override def typeArguments: List[scala.reflect.Manifest[_]] = List(summon[scala.reflect.Manifest[T]])
        override def arrayManifest: scala.reflect.Manifest[Array[Array[T]]] = 
          new scala.reflect.Manifest[Array[Array[T]]] {
            override def runtimeClass: Class[_] = java.lang.reflect.Array.newInstance(runtimeClass, 0).getClass
            override def typeArguments: List[scala.reflect.Manifest[_]] = List(this)
            override def arrayManifest: scala.reflect.Manifest[Array[Array[Array[T]]]] = ???
            override def erasure: Class[_] = runtimeClass
          }
        override def erasure: Class[_] = runtimeClass
      }
    override def erasure: Class[_] = runtimeClass
  }

import java.io.{FileNotFoundException, IOException}
import java.nio.file.{Files, Path, Paths}
import scala.jdk.StreamConverters._
import scala.util.{Failure, Success, Try}

/**
 * Live 2D Cubism avatar JSON setting reader.
 *
 * Read settings from a directory contains the `model3.json`, and other related file.
 *
 * @param directory Directory contains the avatar settings.
 */
class JsonSettingsReader(directory: String) extends SettingsReader {
 private given formats: Formats = DefaultFormats + MotionFile.motionFileSerializer

  override def loadSettings(): Try[Settings] =
    for
      settings <- loadMainModelSettings()
      mocFile <- parseMocFile(settings)
      physics <- parsePhysics(settings)
      textureFiles <- parseTextureFiles(settings)
      pose <- parsePose(settings)
      eyeBlinkParameterIds <- parseEyeBlinkParameterIds(settings)
      lipSyncParameterIds <- parseLipSyncParameterIds(settings)
      expressions <- parseExpressions(settings)
      motionGroups <- parseMotionGroups(settings)
    yield Settings(
      mocFile, textureFiles, physics, pose,
      eyeBlinkParameterIds, lipSyncParameterIds,
      expressions, motionGroups,
      settings.hitAreas
    )

  /**
   * Load and parse the main .model3.json file.
   *
   * @return [[scala.util.Success]] if model loaded correctly, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def loadMainModelSettings(): Try[ModelSetting] =
    for
      directory <- findModelDirectory()
      jsonContent <- loadMainJsonFile(directory)
      parsedJson <- Try(parse(jsonContent))
    yield {
      // 使用原始解析方式，但处理MotionFile的序列化问题
      parsedJson.camelizeKeys.extract[ModelSetting]
    }

  /**
   * Validate the path avatar directory exist.
   *
   * @return [[scala.util.Success]] if directory exist, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def findModelDirectory(): Try[Path] =
    val directoryPath = Paths.get(directory)

    if Files.notExists(directoryPath) then
      Failure(new FileNotFoundException(s"The folder $directory does not exist."))
    else
      Success(directoryPath)

  /**
   * Load main .model3.json file to a String.
   *
   * @param directoryPath The directory path contains the .model3.json file.
   *
   * @return [[scala.util.Success]] containing the JSON file content, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def loadMainJsonFile(directoryPath: Path): Try[String] =
    def isMainModel(path: Path): Boolean = path.getFileName.toString.endsWith(".model3.json")

    Try(Files.list(directoryPath))
      .flatMap { files =>
        files.toScala(LazyList)
          .find(p => isMainModel(p) && p.isReadableFile)
          .toRight(new FileNotFoundException(s"Main model json file not found at $directory"))
          .toTry
      }
      .flatMap(p => p.readToString())
      .recoverWith { case e: FileNotFoundException =>
        Failure(new FileNotFoundException(s"Main model json file not found at $directory: ${e.getMessage}"))
      }
      .recoverWith { case e: IOException =>
        Failure(new IOException(s"Failed to read main model json file at $directory: ${e.getMessage}"))
      }

  /**
   * Parse moc file location
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing absolute path of .moc file, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parseMocFile(modelSetting: ModelSetting): Try[String] =
    val filePath = s"$directory/${modelSetting.fileReferences.moc}"
    val path = Paths.get(filePath)

    if path.toFile.exists() && path.isReadableFile then
      Success(path.toAbsolutePath.toString)
    else
      Failure(new FileNotFoundException(s"Moc file not found or not readable: $filePath"))

  /**
   * Parse texture files location.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing list of absolute path of texture files, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parseTextureFiles(modelSetting: ModelSetting): Try[List[String]] = Try {
    modelSetting.fileReferences
      .textures
      .view
      .map(file => Paths.get(s"$directory/$file"))
      .filter(_.isReadableFile)
      .map(_.toAbsolutePath.toString)
      .toList
    }

  /**
   * Parse eye blink parameters.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing list of parameters related to eye blink, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parseEyeBlinkParameterIds(modelSetting: ModelSetting): Try[List[String]] = Try {

    def isEyeBlinkParameter(group: Group): Boolean = {
      group.name == "EyeBlink" && group.target == "Parameter"
    }

    for {
      group <- modelSetting.groups if isEyeBlinkParameter(group)
      parameterId <- group.ids
    } yield {
      parameterId
    }
  }

  /**
   * Parse lip sync parameters.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing list of parameters related to lip sync, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parseLipSyncParameterIds(modelSetting: ModelSetting): Try[List[String]] = Try {

    def isLipSyncParameter(group: Group): Boolean = {
      group.name == "LipSync" && group.target == "Parameter"
    }

    for {
      group <- modelSetting.groups if isLipSyncParameter(group)
      parameterId <- group.ids
    } yield {
      parameterId
    }
  }

  /**
   * Parse Pose settings.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing optional pose settings, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parsePose(modelSetting: ModelSetting): Try[Option[PoseSetting]] = Try {
    for {
      pose <- modelSetting.fileReferences.pose
      jsonFile <- Option(Paths.get(s"$directory/$pose")) if jsonFile.isReadableFile
      jsonFileContent <- jsonFile.readToString().toOption
      json <- Try(parse(jsonFileContent)).toOption
    } yield {
      val camelized = json.camelizeKeys
      val fadeInTime = (camelized \ "fadeInTime").extractOpt[Double].map(_.toFloat)
      val groups = (camelized \ "groups") match {
        case JArray(groupList) =>
          groupList.map {
            case JArray(parts) =>
              parts.map { partJson =>
                val partCamel = partJson.camelizeKeys
                val id = (partCamel \ "id").extract[String]
                val links = (partCamel \ "link").extractOpt[List[String]].getOrElse(Nil)
                PoseSetting.Part(id, links)
              }
            case _ => Nil
          }
        case _ => Nil
      }
      PoseSetting(fadeInTime, groups)
    }
  }

  /**
   * Parse Physics settings.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing optional physics settings, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parsePhysics(modelSetting: ModelSetting): Try[Option[PhysicsSetting]] = Try {
    for {
      physics <- modelSetting.fileReferences.physics
      jsonFile <- Option(Paths.get(s"$directory/$physics")) if jsonFile.isReadableFile
      jsonFileContent <- jsonFile.readToString().toOption
    } yield {
      parse(jsonFileContent).camelizeKeys.extract[PhysicsSetting]
    }
  }

  /**
   * Parse Expression settings.
   *
   * The value inside returned [[scala.util.Success]] object, will be a map that key is expression name,
   * value is the setting of that expression.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing expression settings, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parseExpressions(modelSetting: ModelSetting): Try[Map[String, ExpressionSetting]] = Try {
    val nameToExpressionList = for
      expressionFileInfo <- modelSetting.fileReferences.expressions
      expressionJsonFilePath = Paths.get(s"$directory/${expressionFileInfo.file}")
      jsonFile <- Option(expressionJsonFilePath) if jsonFile.isReadableFile
      jsonFileContent <- jsonFile.readToString().toOption
      parsedJson <- Try(parse(jsonFileContent)).toOption
    yield
      expressionFileInfo.name -> ExpressionSetting.fromJson(parsedJson)
    
    nameToExpressionList.toMap
  }

  /**
   * Parse Motion group settings.
   *
   * The value inside returned [[scala.util.Success]] object, will be a map that key is name of motion group,
   * value is the list of settings of motions in that group.
   *
   * @param modelSetting  The model setting object.
   *
   * @return [[scala.util.Success]] containing map of motion settings, otherwise [[scala.util.Failure]] denoted the exception.
   */
  private def parseMotionGroups(modelSetting: ModelSetting): Try[Map[String, List[MotionSetting]]] = Try {
    // 手动解析motions以避免Scala 3序列化问题
    val motionsMap = for (groupName, motionList) <- modelSetting.fileReferences.motions yield {
      val motionJsonList = for
        motionFile <- motionList
        paredJson <- motionFile.loadMotion(directory).toOption
      yield
        MotionSetting(
          paredJson.version,
          motionFile.fadeInTime,
          motionFile.fadeOutTime,
          motionFile.sound.map(directory + "/" + _),
          paredJson.meta,
          paredJson.userData,
          paredJson.curves
        )
      
      groupName -> motionJsonList
    }
    motionsMap
  }

}
