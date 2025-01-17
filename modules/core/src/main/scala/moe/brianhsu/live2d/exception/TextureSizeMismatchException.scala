package moe.brianhsu.live2d.exception

class TextureSizeMismatchException(val expectedSize: Int) extends Exception(s"The texture file list size does not match information in the Live2D Model, expectedSize = $expectedSize")
