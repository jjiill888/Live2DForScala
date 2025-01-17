package moe.brianhsu.utils.expectation

import scala.io.Source

case class ExpectedDrawableBasic(id: String, constFlags: Byte, dynamicFlags: Byte, drawOrder: Int,
                                 renderOrder: Int, opacity: Float, textureIndex: Int, masksSize: Int,
                                 indexSize: Int, positionsSize: Int, textureCoordinatesSize: Int, parentPartIndex: Int)

object ExpectedDrawableBasic {
  def getList: List[ExpectedDrawableBasic] = {
    val lines = Source.fromResource("expectation/drawableBasicList.txt").getLines().drop(1)
    lines.map { line =>

      val Array(
        id, constFlags, dynamicFlags, drawOrder, renderOrder, opacity,
        textureIndex, masksSize, indexSize, positionsSize, textureCoordinatesSize, parentPartIndex
      ) = line.split(" ")

      ExpectedDrawableBasic(
        id, constFlags.toByte, dynamicFlags.toByte, drawOrder.toInt, renderOrder.toInt,
        opacity.toFloat, textureIndex.toInt, masksSize.toInt, indexSize.toInt,
        positionsSize.toInt, textureCoordinatesSize.toInt, parentPartIndex.toInt
      )

    }.toList
  }

}

