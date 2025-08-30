package moe.brianhsu.live2d.enitiy.avatar.settings.detail

import ExpressionSetting.Parameters
import org.json4s.JsonAST.{JDouble, JInt, JString}
import org.json4s.{DefaultFormats, Formats, JValue}
import org.json4s.MonadicJValue.jvalueToMonadic

object ExpressionSetting {
  /**
   * The parameter of a expression.
   * @param id  The parameter id.
   * @param value The target value of this parameter.
   * @param blend The blend type, possible value are `Add`, `Multiply`, `Overwrite`.
   */
  case class Parameters(id: String, value: Float, blend: Option[String])

  private given formats: Formats = DefaultFormats

  /**
   * Safely convert a JSON value to Float.
   */
  private def toFloat(value: JValue): Option[Float] = value match
    case JDouble(v) => Some(v.toFloat)
    case JInt(v)    => Some(v.toFloat)
    case _          => None

  /**
   * Safely convert a JSON value to String.
   */
  private def toStringOpt(value: JValue): Option[String] = value match
    case JString(s) => Some(s)
    case _          => None

  /**
   * Manually parse expression JSON to avoid ScalaSig lookup issues.
   *
   * @param json Parsed JSON value of an expression file.
   * @return Parsed [[ExpressionSetting]] instance.
   */
  def fromJson(json: JValue): ExpressionSetting =
    val camelized = json.camelizeKeys
    val fadeInTime = toFloat(camelized \ "fadeInTime")
    val fadeOutTime = toFloat(camelized \ "fadeOutTime")
    val parameters = (camelized \ "parameters").children.flatMap { param =>
      for
        id    <- toStringOpt(param \ "id")
        value <- toFloat(param \ "value")
      yield
        val blend = toStringOpt(param \ "blend")
        Parameters(id, value, blend)
    }
    ExpressionSetting(fadeInTime, fadeOutTime, parameters)
}

/**
 * Expression of a Live 2D Cubism Avatar.
 *
 * @param fadeInTime  Optional fade in time, in seconds.
 * @param fadeOutTime Optional fade out time, in seconds.
 * @param parameters  List of parameter target values.
 */
case class ExpressionSetting(
  fadeInTime: Option[Float],
  fadeOutTime: Option[Float],
  parameters: List[Parameters]
)
