package moe.brianhsu.live2d.adapter.gateway.avatar.settings.json.model

import moe.brianhsu.live2d.adapter.RichPath._
import moe.brianhsu.live2d.adapter.gateway.avatar.settings.json.model.Motion
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats, JValue, JObject, JString, JDouble, JNothing}
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

import java.nio.file.Paths
import scala.util.Try

/**
 * Represent the MotionFile object in JSON file.

 * @param file        The file for this motion, in relative path.
 * @param fadeInTime  Optional fade in time, in seconds.
 * @param fadeOutTime Optional fade out time, in seconds.
 * @param sound       Sound of this motion, in relative path.
 */
private[json] case class MotionFile(file: String, fadeInTime: Option[Float] = None, fadeOutTime: Option[Float] = None, sound: Option[String] = None) {

  private given formats: Formats = DefaultFormats

  /**
   * Load this motion from specified directory.
   *
   * @param directory The directory contain the motion file.
   * @return [[scala.util.Success]] if loaded successfully, [[scala.util.Failure]] otherwise.
   */
  def loadMotion(directory: String): Try[Motion] = {
    for {
      path <- Try(Paths.get(s"$directory/$file")) if path.isReadableFile
      jsonFileContent <- path.readToString()
      parsedJson <- Try(parse(jsonFileContent).camelizeKeys.extract[Motion])
    } yield {
      parsedJson
    }
  }
}


