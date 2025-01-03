package moe.brianhsu.live2d.enitiy.model.parameter

import com.sun.jna.Pointer
import moe.brianhsu.live2d.exception.ParameterInvalidException

/**
 * This class represent parameters of a Live 2D Cubism model.
 *
 * @param pointer         The pointer to the actual memory address of current value of this parameter.
 * @param id              The parameter id.
 * @param parameterType   The parameter type.
 * @param min             The minimum value of this parameter.
 * @param max             The maximum value of this parameter.
 * @param default         The default value of this parameter.
 */
case class CPointerParameter(
                              private val pointer: Pointer,
                              override val id: String,
                              override val parameterType: ParameterType,
                              override val min: Float,
                              override val max: Float,
                              override val default: Float,
                              override val keyValues: List[Float]) extends Parameter {

  /**
   * Get the current value of this parameter.
   *
   * @return The current value of this parameter.
   */
  override def current: Float = pointer.getFloat(0)

  /**
   * Update this parameter to a new value.
   *
   * @param value The new value to assign.
   * @throws moe.brianhsu.live2d.exception.ParameterInvalidException if the assigned value is invalid.
   */
  override def doUpdateValue(value: Float): Unit = {

    if (value < min || value > max) {
      throw new ParameterInvalidException(id, value, min, max)
    }

    pointer.setFloat(0, value)
  }
}
