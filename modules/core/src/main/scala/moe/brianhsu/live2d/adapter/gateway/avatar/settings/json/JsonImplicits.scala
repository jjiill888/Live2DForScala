package moe.brianhsu.live2d.adapter.gateway.avatar.settings.json

import org.json4s.{JField, JValue}

object JsonImplicits {
  private def camelize(name: String): String = {
    val parts = name.split("_").filter(_.nonEmpty)
    if (parts.isEmpty) name
    else parts.head + parts.tail.map(_.capitalize).mkString
  }

  extension (jv: JValue)
    def camelizeKeys: JValue =
      jv.transformField { case JField(n, v) => JField(camelize(n), v.camelizeKeys) }
}
