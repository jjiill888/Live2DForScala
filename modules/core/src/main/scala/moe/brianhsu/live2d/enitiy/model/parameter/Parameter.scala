package moe.brianhsu.live2d.enitiy.model.parameter

import moe.brianhsu.live2d.enitiy.model.parameter.PerformanceOptimizations.*

trait Parameter {
  /**
   * The parameter id.
   */
  val id: String

  /**
   * The parameter type
   */
  val parameterType: ParameterType

  /**
   * The minimum value of this parameter
   */
  val min: Float

  /**
   * The maximum value of this parameter.
   */
  val max: Float

  /**
   * The default value of this parameter.
   */
  val default: Float

  /**
   * The key values of this parameter
   */
  val keyValues: List[Float]

  /**
   * Get the current value of this parameter.
   *
   * @return The current value of this parameter.
   */
  def current: Float

  /**
   * Update this parameter to a new value.
   *
   * @param value The new value to assign.
   * @throws moe.brianhsu.live2d.exception.ParameterInvalidException if the assigned value is invalid.
   */
  protected def doUpdateValue(value: Float): Unit

  /**
   * Update to new value with weight and truncation.
   *
   * This method will update the value of this parameter to the assigned value.
   *
   * If the assigned value after weighted is less or large than the [[min]] or [[max]]
   * value, it will use [[min]] or [[max]] value, instead of assigned value.
   *
   * @param value   The new value of this parameter.
   * @param weight  The weight of the assigned value.
   */
  inline def update(value: Float, weight: Float = 1.0f): Unit =
    val valueFitInRange = clamp(value, this.min, this.max)
    if weight == 1.0f then
      doUpdateValue(valueFitInRange)
    else
      doUpdateValue(interpolate(this.current, valueFitInRange, weight))

  /**
   * Add a weighted value to the current value.
   *
   * @param value   The value to added.
   * @param weight  The weight of `value`.
   */
  inline def add(value: Float, weight: Float = 1.0f): Unit =
    update(this.current + (value * weight))

  /**
   * Multiply a weighted value to the current value.
   *
   * @param value   The value to added.
   * @param weight  The weight of `value`.
   */
  inline def multiply(value: Float, weight: Float = 1.0f): Unit =
    update(this.current * (1.0f + (value - 1.0f) * weight))
}
