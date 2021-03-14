package com.live2d.core.types

import com.live2d.core.CsmVector
import com.sun.jna.{Pointer, PointerType}

class PointerToArrayOfArrayOfCsmVector(pointer: Pointer) extends PointerType(pointer) {
  private lazy val arrays = this.getPointer.getPointerArray(0)
  private var cachedData: Map[(Int, Int), CsmVector] = Map.empty
  def this() = this(null)
  def apply(row: Int)(column: Int): CsmVector = {
    val key = (row, column)
    val pointer = arrays(row).share(column * CsmVector.SIZE)
    val data = cachedData.getOrElse(key, new CsmVector(pointer))
    cachedData = cachedData.updated(key, data)
    data
  }
}