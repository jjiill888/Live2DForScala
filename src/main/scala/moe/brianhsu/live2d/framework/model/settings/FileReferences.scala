package moe.brianhsu.live2d.framework.model.settings

case class FileReferences(moc: String, textures: List[String],
                          physics: Option[String],
                          pose: Option[String],
                          expressions: List[ExpressionFile],
                          motions: Map[String, List[MotionFile]],
                          userData: Option[String])