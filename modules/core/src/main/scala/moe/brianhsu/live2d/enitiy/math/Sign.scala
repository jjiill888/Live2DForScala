package moe.brianhsu.live2d.enitiy.math

sealed trait Sign
case object Positive extends Sign
case object Negative extends Sign
case object Neutral extends Sign

object Sign:
  // Factory method with improved Scala 3 syntax
  def apply(value: Float): Sign = 
    if value == 0 then Neutral
    else if value < 0 then Negative
    else Positive
  
  // Extension methods for enhanced functionality
  extension (sign: Sign)
    def isPositive: Boolean = sign == Positive
    def isNegative: Boolean = sign == Negative
    def isNeutral: Boolean = sign == Neutral
