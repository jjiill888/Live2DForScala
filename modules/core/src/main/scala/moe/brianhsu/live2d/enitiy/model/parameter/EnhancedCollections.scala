package moe.brianhsu.live2d.enitiy.model.parameter

import scala.collection.mutable
import scala.util.Try

/**
 * Enhanced collection operations using Scala 3 extension methods.
 * This demonstrates how to add functionality to existing collection types.
 */
object EnhancedCollections:
  
  // Extension methods for enhanced List operations
  extension [T](list: List[T])
    /**
     * Safely get element at index with bounds checking
     */
    def getSafely(index: Int): Option[T] =
      if index >= 0 && index < list.length then Some(list(index)) else None
    
    /**
     * Find element with predicate and return index
     */
    def findIndex(predicate: T => Boolean): Option[Int] =
      list.zipWithIndex.find((elem, _) => predicate(elem)).map(_._2)
    
    /**
     * Split list into chunks of specified size
     */
    def chunked(chunkSize: Int): List[List[T]] =
      if chunkSize <= 0 then List.empty
      else list.grouped(chunkSize).toList
    
    /**
     * Remove duplicates while preserving order
     */
    def distinctBy[K](key: T => K): List[T] =
      val seen = mutable.Set.empty[K]
      list.filter { elem =>
        val k = key(elem)
        if seen.contains(k) then false
        else
          seen.add(k)
          true
      }
    
    /**
     * Safe head operation
     */
    def headOption: Option[T] = list.headOption
    
    /**
     * Safe tail operation
     */
    def tailOption: Option[List[T]] = 
      if list.isEmpty then None else Some(list.tail)

  // Extension methods for enhanced Map operations
  extension [K, V](map: Map[K, V])
    /**
     * Get value with default function
     */
    def getOrElseWith(key: K, default: => V): V =
      map.get(key).getOrElse(default)
    
    /**
     * Transform values while keeping keys
     */
    def mapValues[W](f: V => W): Map[K, W] =
      map.view.mapValues(f).toMap
    
    /**
     * Filter by both key and value
     */
    def filterWith(f: (K, V) => Boolean): Map[K, V] =
      map.filter { case (k, v) => f(k, v) }
    
    /**
     * Safe key removal
     */
    def removeSafely(key: K): Map[K, V] =
      map - key
    
    /**
     * Get multiple keys at once
     */
    def getMultiple(keys: Iterable[K]): Map[K, Option[V]] =
      keys.map(k => k -> map.get(k)).toMap

  // Extension methods for enhanced Option operations
  extension [T](option: Option[T])
    /**
     * Transform value if present, otherwise return default
     */
    def mapOrElse[U](f: T => U, default: => U): U =
      option.map(f).getOrElse(default)
    
    /**
     * Execute side effect if present
     */
    def foreach(f: T => Unit): Unit = option.foreach(f)
    
    /**
     * Convert to Try
     */
    def toTry(exception: => Exception): Try[T] =
      option.toRight(exception).toTry
    
    /**
     * Filter with predicate
     */
    def filter(predicate: T => Boolean): Option[T] = option.filter(predicate)
    
    /**
     * Flat map with error handling
     */
    def flatMapSafely[U](f: T => Option[U]): Option[U] =
      option.flatMap(f)

  // Extension methods for enhanced String operations
  extension (str: String)
    /**
     * Check if string is not empty and not null
     */
    def isNonEmpty: Boolean = str != null && str.nonEmpty
    
    /**
     * Safe substring with bounds checking
     */
    def substringSafely(start: Int, end: Int): Option[String] =
      if start >= 0 && end <= str.length && start <= end then
        Some(str.substring(start, end))
      else None
    
    /**
     * Split and filter empty strings
     */
    def splitNonEmpty(delimiter: String): List[String] =
      str.split(delimiter).filter(_.nonEmpty).toList
    
    /**
     * Convert to Option if non-empty
     */
    def toOption: Option[String] =
      if str.isNonEmpty then Some(str) else None
    
    /**
     * Truncate string to specified length
     */
    def truncate(maxLength: Int): String =
      if str.length <= maxLength then str
      else str.take(maxLength) + "..."

  // Extension methods for enhanced numeric operations
  extension (value: Float)
    /**
     * Clamp value between min and max
     */
    def clamp(min: Float, max: Float): Float =
      if value < min then min
      else if value > max then max
      else value
    
    /**
     * Check if value is within range
     */
    def isInRange(min: Float, max: Float): Boolean =
      value >= min && value <= max
    
    /**
     * Linear interpolation
     */
    def lerp(other: Float, t: Float): Float =
      value + (other - value) * t
    
    /**
     * Convert to percentage
     */
    def toPercentage: Float = value * 100.0f
    
    /**
     * Round to specified decimal places
     */
    def roundTo(decimals: Int): Float =
      val factor = math.pow(10, decimals).toFloat
      (value * factor).round / factor

  // Extension methods for enhanced Int operations
  extension (value: Int)
    /**
     * Clamp value between min and max
     */
    def clamp(min: Int, max: Int): Int =
      if value < min then min
      else if value > max then max
      else value
    
    /**
     * Check if value is even
     */
    def isEven: Boolean = value % 2 == 0
    
    /**
     * Check if value is odd
     */
    def isOdd: Boolean = value % 2 != 0
    
    /**
     * Convert to range
     */
    def toRange: Range = 0 until value
    
    /**
     * Times operation with side effect
     */
    def times(f: => Unit): Unit =
      (0 until value).foreach(_ => f)

  // Utility functions using extension methods
  def processParameterList(parameters: List[Float]): List[Float] =
    parameters
      .map(_.clamp(0.0f, 1.0f))
      .distinctBy(_.roundTo(2))
      .filter(_.isInRange(0.1f, 0.9f))

  def safeStringOperation(text: String): Option[String] =
    text.toOption
      .filter(_.isNonEmpty)
      .map(_.truncate(100))
      .filter(_.length > 5)

  def enhancedMapOperation(data: Map[String, Float]): Map[String, Float] =
    data
      .filterWith((_, v) => v.isInRange(0.0f, 1.0f))
      .view.mapValues(_.roundTo(3)).toMap
      .removeSafely("invalid_key")
