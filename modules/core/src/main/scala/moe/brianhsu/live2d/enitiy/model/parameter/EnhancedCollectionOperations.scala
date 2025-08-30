package moe.brianhsu.live2d.enitiy.model.parameter

import scala.collection.immutable.LazyList
import scala.util.{Try, Success, Failure}
import java.nio.file.{Path, Paths}
import scala.io.Source
import moe.brianhsu.live2d.adapter.RichPath._

/**
 * Enhanced Collection Operations for Live2D Parameter Processing
 * 
 * This file demonstrates Scala 3 collection optimizations including:
 * - LazyList for memory-efficient processing
 * - New collection APIs and methods
 * - Improved performance patterns
 * - Memory optimization techniques
 */
object EnhancedCollectionOperations:

  // ============================================================================
  // LazyList Optimizations for Large Data Processing
  // ============================================================================

  /**
   * Process large parameter files using LazyList for memory efficiency
   * Instead of loading all data into memory at once, process it lazily
   */
  def processLargeParameterFile(filePath: String): LazyList[Float] =
    LazyList.from(Source.fromFile(filePath).getLines())
      .filter(_.trim.nonEmpty)
      .map(_.toFloat)
      .filter(_ >= 0.0f)

  /**
   * Memory-efficient parameter validation using LazyList
   * Only validates parameters as they are accessed
   */
  def validateParametersLazily(parameters: LazyList[Float]): LazyList[Try[Float]] =
    parameters.map { param =>
      if param >= 0.0f && param <= 1.0f then
        Success(param)
      else
        Failure(new IllegalArgumentException(s"Parameter $param is out of range [0.0, 1.0]"))
    }

  /**
   * Chunked processing for large parameter sets
   * Processes data in chunks to balance memory usage and performance
   */
  def processParametersInChunks(parameters: LazyList[Float], chunkSize: Int): LazyList[List[Float]] =
    LazyList.from(parameters.grouped(chunkSize).map(_.toList))

  // ============================================================================
  // New Collection API Optimizations
  // ============================================================================

  /**
   * Optimized texture file processing using new collection methods
   * Uses view for lazy evaluation and improved performance
   */
  def processTextureFilesOptimized(files: List[String], directory: String): List[String] =
    files.view
      .map(file => Paths.get(s"$directory/$file"))
      .filter(_.isReadableFile)
      .map(_.toAbsolutePath.toString)
      .toList

  /**
   * Memory-efficient motion processing using LazyList
   * Processes motion files one by one instead of loading all at once
   */
  def processMotionFilesLazily(motionFiles: LazyList[String], directory: String): LazyList[Try[String]] =
    motionFiles.map { file =>
      Try {
        val path = Paths.get(s"$directory/$file")
        if path.isReadableFile then
          path.toAbsolutePath.toString
        else
          throw new IllegalArgumentException(s"File not readable: $file")
      }
    }

  /**
   * Optimized expression processing using new collection features
   * Uses improved for expressions and view for better performance
   */
  def processExpressionsOptimized(expressions: Map[String, String], directory: String): Map[String, Try[String]] =
    expressions.view.mapValues { expressionFile =>
      Try {
        val path = Paths.get(s"$directory/$expressionFile")
        if path.isReadableFile then
          path.toAbsolutePath.toString
        else
          throw new IllegalArgumentException(s"Expression file not readable: $expressionFile")
      }
    }.toMap

  // ============================================================================
  // Performance Optimizations with New Collection Methods
  // ============================================================================

  /**
   * Optimized parameter filtering using new collection methods
   * Uses improved filtering and mapping for better performance
   */
  def filterAndTransformParameters(parameters: List[Float]): List[Float] =
    parameters.view
      .filter(_ >= 0.0f)
      .map(_ * 100.0f)
      .filter(_ > 10.0f)
      .toList

  /**
   * Memory-efficient parameter batch processing
   * Uses LazyList for large parameter sets
   */
  def processParameterBatch(parameters: LazyList[Float]): LazyList[Float] =
    parameters
      .filter(_ >= 0.0f)
      .map(_ * 100.0f)
      .filter(_ > 10.0f)
      .map(value => (math.round(value * 10.0) / 10.0).toFloat)

  /**
   * Optimized effect processing using new collection features
   * Uses improved for expressions and lazy evaluation
   */
  def processEffectsOptimized(effects: List[String]): List[String] =
    effects.view
      .filter(_.nonEmpty)
      .map(_.toLowerCase)
      .toList
      .distinct

  // ============================================================================
  // Memory Optimization Techniques
  // ============================================================================

  /**
   * Memory-efficient file reading using LazyList
   * Reads files line by line instead of loading entire content
   */
  def readFileLazily(filePath: String): LazyList[String] =
    LazyList.from(Source.fromFile(filePath).getLines())
      .filter(_.trim.nonEmpty)

  /**
   * Optimized parameter calculation using view
   * Avoids creating intermediate collections
   */
  def calculateParametersOptimized(parameters: List[Float]): List[Float] =
    parameters.view
      .map(_ * 2.0f)
      .map(_ + 1.0f)
      .map(math.min(_, 100.0f))
      .toList

  /**
   * Memory-efficient data transformation pipeline
   * Uses LazyList for large data processing
   */
  def transformDataPipeline(data: LazyList[String]): LazyList[Float] =
    data
      .filter(_.nonEmpty)
      .map(_.trim)
      .filter(_.matches("\\d+(\\.\\d+)?"))
      .map(_.toFloat)
      .filter(_ >= 0.0f)

  // ============================================================================
  // Advanced Collection Operations
  // ============================================================================

  /**
   * Optimized parameter grouping using new collection methods
   * Groups parameters efficiently using view and improved methods
   */
  def groupParametersOptimized(parameters: List[Float]): Map[String, List[Float]] =
    parameters.view
      .groupBy { param =>
        if param < 0.3f then "low"
        else if param < 0.7f then "medium"
        else "high"
      }
      .view.mapValues(_.toList)
      .toMap

  /**
   * Memory-efficient parameter statistics calculation
   * Uses LazyList for large datasets
   */
  def calculateParameterStats(parameters: LazyList[Float]): (Float, Float, Float) =
    val (sum, count, min, max) = parameters.foldLeft((0.0f, 0, Float.MaxValue, Float.MinValue)) {
      case ((accSum, accCount, accMin, accMax), param) =>
        (accSum + param, accCount + 1, math.min(accMin, param), math.max(accMax, param))
    }
    (sum / count, min, max)

  /**
   * Optimized parameter validation using new collection features
   * Uses improved error handling and collection methods
   */
  def validateParametersWithErrors(parameters: List[Float]): (List[Float], List[String]) =
    val results = parameters.view
      .map { param =>
        if param >= 0.0f && param <= 1.0f then
          Right(param)
        else
          Left(s"Parameter $param is out of range [0.0, 1.0]")
      }
      .toList
    
    val (errors, valid) = results.partitionMap(identity)
    (valid, errors)

  // ============================================================================
  // Utility Methods for Collection Operations
  // ============================================================================

  /**
   * Safe collection operations with error handling
   * Uses Try for safe collection transformations
   */
  def safeCollectionTransform[A, B](collection: List[A])(transform: A => Try[B]): List[B] =
    collection.view
      .flatMap(transform(_).toOption)
      .toList

  /**
   * Memory-efficient collection concatenation
   * Uses LazyList for large collections
   */
  def concatenateCollectionsLazily[A](collections: LazyList[List[A]]): LazyList[A] =
    collections.flatten

  /**
   * Optimized collection deduplication
   * Uses view and distinct for better performance
   */
  def deduplicateOptimized[A](collection: List[A]): List[A] =
    collection.view.distinct.toList

  /**
   * Memory-efficient collection sorting
   * Uses view for lazy sorting
   */
  def sortOptimized[A](collection: List[A])(implicit ordering: Ordering[A]): List[A] =
    collection.view.sorted.toList

  // ============================================================================
  // Performance Monitoring and Optimization
  // ============================================================================

  /**
   * Measure collection operation performance
   * Useful for identifying bottlenecks
   */
  def measurePerformance[A, B](operation: String, collection: List[A])(transform: List[A] => B): B =
    val startTime = System.nanoTime()
    val result = transform(collection)
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1_000_000.0 // Convert to milliseconds
    println(s"$operation took ${duration}ms for ${collection.size} elements")
    result

  /**
   * Memory usage estimation for collection operations
   * Helps in choosing appropriate collection types
   */
  def estimateMemoryUsage[A](collection: List[A]): Long =
    // Rough estimation: 8 bytes per reference + object overhead
    collection.size * 8L

  // ============================================================================
  // Example Usage and Best Practices
  // ============================================================================

  /**
   * Example: Processing large parameter files efficiently
   */
  def processLargeParameterFileExample(filePath: String): Unit =
    val parameters = processLargeParameterFile(filePath)
    val validParameters = validateParametersLazily(parameters)
    val processedParameters = processParameterBatch(validParameters.map(_.getOrElse(0.0f)))
    
    // Process only first 1000 parameters to demonstrate lazy evaluation
    processedParameters.take(1000).foreach(println)

  /**
   * Example: Optimized texture processing
   */
  def processTextureFilesExample(files: List[String], directory: String): Unit =
    val processedFiles = processTextureFilesOptimized(files, directory)
    val validFiles = processedFiles.filter(_.nonEmpty)
    println(s"Processed ${validFiles.size} texture files")

  /**
   * Example: Memory-efficient data transformation
   */
  def transformDataExample(data: List[String]): Unit =
    val lazyData = LazyList.from(data)
    val transformed = transformDataPipeline(lazyData)
    val stats = calculateParameterStats(transformed)
    println(s"Parameter statistics: avg=${stats._1}, min=${stats._2}, max=${stats._3}")

end EnhancedCollectionOperations
