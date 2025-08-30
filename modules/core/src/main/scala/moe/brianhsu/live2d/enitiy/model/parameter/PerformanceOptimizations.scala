package moe.brianhsu.live2d.enitiy.model.parameter

import scala.math.{min, max, abs, sqrt, sin, cos, tan, atan2, pow, log, exp}
import scala.util.Try

/**
 * Performance Optimizations for Live2D Parameter Processing
 * 
 * This file demonstrates Scala 3 performance optimizations using:
 * - inline for hotspot code optimization
 * - transparent inline for type conversion optimization
 * - Compile-time optimizations for mathematical operations
 * - Performance-critical rendering operations
 */
object PerformanceOptimizations:

  // ============================================================================
  // Inline Optimizations for Hotspot Code
  // ============================================================================

  /**
   * Inline optimization for parameter value clamping
   * Eliminates method call overhead for frequently called operations
   */
  inline def clamp(value: Float, min: Float, max: Float): Float =
    if value < min then min
    else if value > max then max
    else value

  /**
   * Inline optimization for parameter interpolation
   * Optimizes weight-based interpolation calculations
   */
  inline def interpolate(current: Float, target: Float, weight: Float): Float =
    if weight >= 1.0f then target
    else current + (target - current) * weight

  /**
   * Inline optimization for parameter validation
   * Fast validation without object allocation
   */
  inline def isValidParameter(value: Float, min: Float, max: Float): Boolean =
    value >= min && value <= max && !value.isNaN && !value.isInfinite

  /**
   * Inline optimization for parameter normalization
   * Fast normalization without branching
   */
  inline def normalizeParameter(value: Float, min: Float, max: Float): Float =
    (value - min) / (max - min)

  /**
   * Inline optimization for parameter denormalization
   * Fast denormalization without branching
   */
  inline def denormalizeParameter(normalized: Float, min: Float, max: Float): Float =
    normalized * (max - min) + min

  /**
   * Inline optimization for smooth interpolation
   * Uses smoothstep function for better visual results
   */
  inline def smoothInterpolate(t: Float): Float =
    t * t * (3.0f - 2.0f * t)

  /**
   * Inline optimization for exponential interpolation
   * Fast exponential interpolation for motion curves
   */
  inline def expInterpolate(current: Float, target: Float, factor: Float): Float =
    current + (target - current) * (1.0f - exp(-factor).toFloat)

  // ============================================================================
  // Transparent Inline for Type Conversion Optimization
  // ============================================================================

  /**
   * Transparent inline for safe float conversion
   * Compile-time type checking with runtime safety
   */
  transparent inline def safeFloat(value: Any): Float =
    inline value match
      case f: Float => f
      case d: Double => d.toFloat
      case i: Int => i.toFloat
      case l: Long => l.toFloat
      case s: String => s.toFloat
      case _ => throw new IllegalArgumentException(s"Cannot convert $value to Float")

  /**
   * Transparent inline for safe int conversion
   * Compile-time type checking with runtime safety
   */
  transparent inline def safeInt(value: Any): Int =
    inline value match
      case i: Int => i
      case l: Long => l.toInt
      case f: Float => f.toInt
      case d: Double => d.toInt
      case s: String => s.toInt
      case _ => throw new IllegalArgumentException(s"Cannot convert $value to Int")

  /**
   * Transparent inline for safe boolean conversion
   * Compile-time type checking with runtime safety
   */
  transparent inline def safeBoolean(value: Any): Boolean =
    inline value match
      case b: Boolean => b
      case i: Int => i != 0
      case f: Float => f != 0.0f
      case s: String => s.toBoolean
      case _ => throw new IllegalArgumentException(s"Cannot convert $value to Boolean")

  /**
   * Transparent inline for safe string conversion
   * Compile-time type checking with runtime safety
   */
  transparent inline def safeString(value: Any): String =
    inline value match
      case s: String => s
      case i: Int => i.toString
      case f: Float => f.toString
      case d: Double => d.toString
      case b: Boolean => b.toString
      case _ => value.toString

  // ============================================================================
  // Mathematical Operations Optimization
  // ============================================================================

  /**
   * Inline optimization for vector magnitude calculation
   * Optimized for 2D vectors commonly used in Live2D
   */
  inline def vectorMagnitude(x: Float, y: Float): Float =
    sqrt(x * x + y * y).toFloat

  /**
   * Inline optimization for vector normalization
   * Fast 2D vector normalization
   */
  inline def normalizeVector(x: Float, y: Float): (Float, Float) =
    val mag = vectorMagnitude(x, y)
    if mag > 0.0f then (x / mag, y / mag) else (0.0f, 0.0f)

  /**
   * Inline optimization for dot product
   * Fast 2D dot product calculation
   */
  inline def dotProduct(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    x1 * x2 + y1 * y2

  /**
   * Inline optimization for cross product magnitude
   * Fast 2D cross product magnitude
   */
  inline def crossProductMagnitude(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    x1 * y2 - y1 * x2

  /**
   * Inline optimization for angle calculation
   * Fast angle calculation between two vectors
   */
  inline def angleBetweenVectors(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    atan2(crossProductMagnitude(x1, y1, x2, y2), dotProduct(x1, y1, x2, y2)).toFloat

  /**
   * Inline optimization for distance calculation
   * Fast Euclidean distance between two points
   */
  inline def distance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toFloat

  /**
   * Inline optimization for squared distance calculation
   * Fast squared distance (avoids sqrt for comparisons)
   */
  inline def squaredDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)

  // ============================================================================
  // Matrix Operations Optimization
  // ============================================================================

  /**
   * Inline optimization for 2x2 matrix multiplication
   * Optimized for transformation matrices
   */
  inline def multiply2x2(
    a11: Float, a12: Float, a21: Float, a22: Float,
    b11: Float, b12: Float, b21: Float, b22: Float
  ): (Float, Float, Float, Float) =
    (
      a11 * b11 + a12 * b21, a11 * b12 + a12 * b22,
      a21 * b11 + a22 * b21, a21 * b12 + a22 * b22
    )

  /**
   * Inline optimization for 2D point transformation
   * Fast point transformation with 2x2 matrix
   */
  inline def transformPoint(
    x: Float, y: Float,
    m11: Float, m12: Float, m21: Float, m22: Float
  ): (Float, Float) =
    (m11 * x + m12 * y, m21 * x + m22 * y)

  /**
   * Inline optimization for 2D point transformation with translation
   * Fast point transformation with 2x2 matrix and translation
   */
  inline def transformPointWithTranslation(
    x: Float, y: Float,
    m11: Float, m12: Float, m21: Float, m22: Float,
    tx: Float, ty: Float
  ): (Float, Float) =
    (m11 * x + m12 * y + tx, m21 * x + m22 * y + ty)

  // ============================================================================
  // Rendering Operations Optimization
  // ============================================================================

  /**
   * Inline optimization for color blending
   * Fast alpha blending for rendering
   */
  inline def blendColors(
    srcR: Float, srcG: Float, srcB: Float, srcA: Float,
    dstR: Float, dstG: Float, dstB: Float, dstA: Float
  ): (Float, Float, Float, Float) =
    val alpha = srcA + dstA * (1.0f - srcA)
    if alpha > 0.0f then
      val invAlpha = 1.0f / alpha
      (
        (srcR * srcA + dstR * dstA * (1.0f - srcA)) * invAlpha,
        (srcG * srcA + dstG * dstA * (1.0f - srcA)) * invAlpha,
        (srcB * srcA + dstB * dstA * (1.0f - srcA)) * invAlpha,
        alpha
      )
    else
      (0.0f, 0.0f, 0.0f, 0.0f)

  /**
   * Inline optimization for color multiplication
   * Fast color multiplication for effects
   */
  inline def multiplyColors(
    r1: Float, g1: Float, b1: Float, a1: Float,
    r2: Float, g2: Float, b2: Float, a2: Float
  ): (Float, Float, Float, Float) =
    (r1 * r2, g1 * g2, b1 * b2, a1 * a2)

  /**
   * Inline optimization for color addition
   * Fast color addition for effects
   */
  inline def addColors(
    r1: Float, g1: Float, b1: Float, a1: Float,
    r2: Float, g2: Float, b2: Float, a2: Float
  ): (Float, Float, Float, Float) =
    (r1 + r2, g1 + g2, b1 + b2, a1 + a2)

  /**
   * Inline optimization for viewport clipping
   * Fast viewport clipping calculations
   */
  inline def clipToViewport(
    x: Float, y: Float,
    viewportX: Float, viewportY: Float,
    viewportWidth: Float, viewportHeight: Float
  ): (Float, Float) =
    (
      clamp(x, viewportX, viewportX + viewportWidth),
      clamp(y, viewportY, viewportY + viewportHeight)
    )

  // ============================================================================
  // Animation and Motion Optimization
  // ============================================================================

  /**
   * Inline optimization for easing functions
   * Fast easing calculations for animations
   */
  inline def easeInOut(t: Float): Float =
    if t < 0.5f then 2.0f * t * t else 1.0f - 2.0f * (1.0f - t) * (1.0f - t)

  /**
   * Inline optimization for ease-in function
   * Fast ease-in calculation
   */
  inline def easeIn(t: Float): Float =
    t * t

  /**
   * Inline optimization for ease-out function
   * Fast ease-out calculation
   */
  inline def easeOut(t: Float): Float =
    1.0f - (1.0f - t) * (1.0f - t)

  /**
   * Inline optimization for bounce function
   * Fast bounce calculation for animations
   */
  inline def bounce(t: Float): Float =
    if t < 1.0f / 2.75f then
      7.5625f * t * t
    else if t < 2.0f / 2.75f then
      7.5625f * (t - 1.5f / 2.75f) * (t - 1.5f / 2.75f) + 0.75f
    else if t < 2.5f / 2.75f then
      7.5625f * (t - 2.25f / 2.75f) * (t - 2.25f / 2.75f) + 0.9375f
    else
      7.5625f * (t - 2.625f / 2.75f) * (t - 2.625f / 2.75f) + 0.984375f

  /**
   * Inline optimization for elastic function
   * Fast elastic calculation for animations
   */
  inline def elastic(t: Float): Float =
    if t == 0.0f then 0.0f
    else if t == 1.0f then 1.0f
    else
      val p = 0.3f
      val s = p / 4.0f
      val pow2 = pow(2.0, -10.0 * t).toFloat
      sin((t - s) * (2.0 * Math.PI) / p).toFloat * pow2 + 1.0f

  // ============================================================================
  // Physics and Effects Optimization
  // ============================================================================

  /**
   * Inline optimization for spring calculation
   * Fast spring physics calculation
   */
  inline def springForce(
    position: Float, target: Float,
    velocity: Float, damping: Float, stiffness: Float
  ): Float =
    val displacement = target - position
    -stiffness * displacement - damping * velocity

  /**
   * Inline optimization for gravity calculation
   * Fast gravity physics calculation
   */
  inline def gravityForce(mass: Float, gravity: Float): Float =
    mass * gravity

  /**
   * Inline optimization for friction calculation
   * Fast friction physics calculation
   */
  inline def frictionForce(velocity: Float, friction: Float): Float =
    -velocity * friction

  /**
   * Inline optimization for wind force calculation
   * Fast wind physics calculation
   */
  inline def windForce(windStrength: Float, windDirection: Float): (Float, Float) =
    (windStrength * cos(windDirection).toFloat, windStrength * sin(windDirection).toFloat)

  // ============================================================================
  // Performance Monitoring and Benchmarking
  // ============================================================================

  /**
   * Inline optimization for performance measurement
   * Fast performance measurement without object allocation
   */
  inline def measureTime[T](operation: String)(block: => T): T =
    val startTime = System.nanoTime()
    val result = block
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1_000_000.0 // Convert to milliseconds
    println(s"$operation took ${duration}ms")
    result

  /**
   * Inline optimization for conditional performance measurement
   * Fast conditional performance measurement
   */
  inline def measureTimeIf[T](condition: Boolean, operation: String)(block: => T): T =
    if condition then measureTime(operation)(block) else block

  /**
   * Inline optimization for memory usage estimation
   * Fast memory usage estimation
   */
  inline def estimateMemoryUsage(size: Int, elementSize: Int): Long =
    size * elementSize.toLong

  // ============================================================================
  // Utility Functions with Inline Optimization
  // ============================================================================

  /**
   * Inline optimization for safe division
   * Fast safe division with zero check
   */
  inline def safeDivide(numerator: Float, denominator: Float, defaultValue: Float = 0.0f): Float =
    if denominator != 0.0f then numerator / denominator else defaultValue

  /**
   * Inline optimization for safe square root
   * Fast safe square root with negative check
   */
  inline def safeSqrt(value: Float): Float =
    if value >= 0.0f then sqrt(value).toFloat else 0.0f

  /**
   * Inline optimization for safe power
   * Fast safe power calculation
   */
  inline def safePow(base: Float, exponent: Float): Float =
    if base >= 0.0f || exponent.toInt == exponent then pow(base, exponent).toFloat else 0.0f

  /**
   * Inline optimization for modulo operation
   * Fast modulo operation for wrapping values
   */
  inline def modulo(value: Float, divisor: Float): Float =
    if divisor != 0.0f then
      val result = value % divisor
      if result < 0.0f then result + abs(divisor) else result
    else value

  /**
   * Inline optimization for wrapping values
   * Fast value wrapping within range
   */
  inline def wrap(value: Float, min: Float, max: Float): Float =
    val range = max - min
    if range > 0.0f then
      min + modulo(value - min, range)
    else value

  // ============================================================================
  // Compile-Time Constants and Configuration
  // ============================================================================

  /**
   * Compile-time constants for performance optimization
   */
  val PI: Float = 3.14159265359f
  val TWO_PI: Float = 2.0f * PI
  val HALF_PI: Float = PI / 2.0f
  val DEG_TO_RAD: Float = PI / 180.0f
  val RAD_TO_DEG: Float = 180.0f / PI

  /**
   * Compile-time constants for physics
   */
  val GRAVITY: Float = 9.81f
  val AIR_RESISTANCE: Float = 0.98f
  val FRICTION: Float = 0.95f

  /**
   * Compile-time constants for rendering
   */
  val MAX_COLOR_VALUE: Float = 255.0f
  val MIN_COLOR_VALUE: Float = 0.0f
  val ALPHA_THRESHOLD: Float = 0.01f

  // ============================================================================
  // Example Usage and Best Practices
  // ============================================================================

  /**
   * Example: Optimized parameter update pipeline
   */
  def updateParameterOptimized(
    currentValue: Float,
    targetValue: Float,
    weight: Float,
    min: Float,
    max: Float
  ): Float =
    val interpolated = interpolate(currentValue, targetValue, weight)
    clamp(interpolated, min, max)

  /**
   * Example: Optimized vector transformation
   */
  def transformVectorOptimized(
    x: Float, y: Float,
    scaleX: Float, scaleY: Float,
    rotation: Float,
    translateX: Float, translateY: Float
  ): (Float, Float) =
    val cosRot = cos(rotation).toFloat
    val sinRot = sin(rotation).toFloat
    val (rotatedX, rotatedY) = transformPoint(x, y, cosRot, -sinRot, sinRot, cosRot)
    val (scaledX, scaledY) = (rotatedX * scaleX, rotatedY * scaleY)
    (scaledX + translateX, scaledY + translateY)

  /**
   * Example: Optimized color blending pipeline
   */
  def blendColorsOptimized(
    baseR: Float, baseG: Float, baseB: Float, baseA: Float,
    overlayR: Float, overlayG: Float, overlayB: Float, overlayA: Float,
    blendMode: String
  ): (Float, Float, Float, Float) =
    blendMode match
      case "normal" => blendColors(baseR, baseG, baseB, baseA, overlayR, overlayG, overlayB, overlayA)
      case "multiply" => multiplyColors(baseR, baseG, baseB, baseA, overlayR, overlayG, overlayB, overlayA)
      case "add" => addColors(baseR, baseG, baseB, baseA, overlayR, overlayG, overlayB, overlayA)
      case _ => (baseR, baseG, baseB, baseA)

end PerformanceOptimizations
