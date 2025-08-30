package moe.brianhsu.live2d.enitiy.model.parameter

import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.io.Source
import java.nio.file.{Path, Paths}
import java.io.{FileNotFoundException, IOException}

/**
 * Enhanced Error Handling for Live2D Parameter Processing
 * 
 * This file demonstrates Scala 3 error handling optimizations including:
 * - Improved Try and Either usage
 * - Better error recovery strategies
 * - Functional error handling patterns
 * - Error composition and chaining
 */
object EnhancedErrorHandling:

  // ============================================================================
  // Enhanced Try Operations
  // ============================================================================

  /**
   * Enhanced Try with better error context
   * Provides more detailed error information
   */
  def loadParameterFileWithContext(filePath: String): Try[List[Float]] =
    Try(Source.fromFile(filePath).getLines().toList)
      .flatMap { lines =>
        Try(lines.map(_.toFloat))
      }
      .recoverWith { case e: NumberFormatException =>
        Failure(new IllegalArgumentException(s"Invalid number format in file $filePath: ${e.getMessage}"))
      }
      .recoverWith { case e: FileNotFoundException =>
        Failure(new FileNotFoundException(s"Parameter file not found: $filePath"))
      }

  /**
   * Try with custom error transformation
   * Transforms specific exceptions into more meaningful errors
   */
  def loadTextureWithCustomError(texturePath: String): Try[Array[Byte]] =
    Try(Paths.get(texturePath))
      .flatMap { path =>
        if path.toFile.exists() then
          Try(java.nio.file.Files.readAllBytes(path))
        else
          Failure(new FileNotFoundException(s"Texture file not found: $texturePath"))
      }
      .recoverWith { case e: IOException =>
        Failure(new RuntimeException(s"Failed to read texture file $texturePath: ${e.getMessage}"))
      }

  /**
   * Try with validation
   * Combines Try with validation logic
   */
  def validateAndLoadParameters(filePath: String): Try[List[Float]] =
    for
      lines <- Try(Source.fromFile(filePath).getLines().toList)
      _ <- validateFileContent(lines)
      parameters <- Try(lines.map(_.toFloat))
      _ <- validateParameters(parameters)
    yield parameters

  private def validateFileContent(lines: List[String]): Try[Unit] =
    if lines.isEmpty then
      Failure(new IllegalArgumentException("Parameter file is empty"))
    else if lines.exists(_.trim.isEmpty) then
      Failure(new IllegalArgumentException("Parameter file contains empty lines"))
    else
      Success(())

  private def validateParameters(parameters: List[Float]): Try[Unit] =
    val invalidParams = parameters.filterNot(param => param >= 0.0f && param <= 1.0f)
    if invalidParams.nonEmpty then
      Failure(new IllegalArgumentException(s"Invalid parameters found: ${invalidParams.mkString(", ")}"))
    else
      Success(())

  // ============================================================================
  // Enhanced Either Operations
  // ============================================================================

  /**
   * Either for parameter validation with detailed error messages
   * Returns either valid parameters or a list of validation errors
   */
  def validateParametersWithErrors(parameters: List[Float]): Either[List[String], List[Float]] =
    val errors = parameters.zipWithIndex.flatMap { case (param, index) =>
      if param < 0.0f then
        Some(s"Parameter at index $index ($param) is negative")
      else if param > 1.0f then
        Some(s"Parameter at index $index ($param) exceeds maximum value 1.0")
      else
        None
    }

    if errors.isEmpty then
      Right(parameters)
    else
      Left(errors)

  /**
   * Either for file processing with multiple error types
   * Handles different types of errors separately
   */
  def processParameterFile(filePath: String): Either[ProcessingError, List[Float]] =
    for
      path <- validateFilePath(filePath)
      content <- readFileContent(path)
      parameters <- parseParameters(content)
      validatedParams <- validateParametersEither(parameters)
    yield validatedParams

  sealed trait ProcessingError
  case class FileNotFoundError(path: String) extends ProcessingError
  case class ParseError(message: String) extends ProcessingError
  case class ValidationError(errors: List[String]) extends ProcessingError
  case class IOError(message: String) extends ProcessingError

  private def validateFilePath(filePath: String): Either[ProcessingError, Path] =
    val path = Paths.get(filePath)
    if path.toFile.exists() then
      Right(path)
    else
      Left(FileNotFoundError(filePath))

  private def readFileContent(path: Path): Either[ProcessingError, String] =
    Try(java.nio.file.Files.readString(path)).toEither.left.map { e =>
      IOError(s"Failed to read file ${path}: ${e.getMessage}")
    }

  private def parseParameters(content: String): Either[ProcessingError, List[Float]] =
    Try(content.linesIterator.map(_.trim).filter(_.nonEmpty).map(_.toFloat).toList).toEither.left.map { e =>
      ParseError(s"Failed to parse parameters: ${e.getMessage}")
    }

  private def validateParametersEither(parameters: List[Float]): Either[ProcessingError, List[Float]] =
    validateParametersWithErrors(parameters).left.map(ValidationError.apply)

  // ============================================================================
  // Error Recovery Strategies
  // ============================================================================

  /**
   * Try with fallback strategy
   * Attempts multiple approaches to load data
   */
  def loadParameterWithFallback(primaryPath: String, fallbackPath: String): Try[List[Float]] =
    loadParameterFileWithContext(primaryPath)
      .recoverWith { case _ =>
        println(s"Primary path failed, trying fallback: $fallbackPath")
        loadParameterFileWithContext(fallbackPath)
      }
      .recoverWith { case e =>
        println(s"All paths failed, using default parameters")
        Success(List.fill(10)(0.5f))
      }

  /**
   * Either with recovery strategy
   * Provides multiple recovery options
   */
  def loadParameterWithRecovery(filePath: String): Either[ProcessingError, List[Float]] =
    processParameterFile(filePath)
      .orElse {
        println(s"Primary file failed, trying backup file")
        processParameterFile(filePath + ".backup")
      }
      .orElse {
        println(s"All files failed, using default parameters")
        Right(List.fill(10)(0.5f))
      }

  // ============================================================================
  // Error Composition and Chaining
  // ============================================================================

  /**
   * Composing multiple Try operations
   * Chains multiple operations with error handling
   */
  def loadAndProcessParameters(filePath: String): Try[ProcessedParameters] =
    for
      rawParams <- loadParameterFileWithContext(filePath)
      validatedParams <- validateAndLoadParameters(filePath)
      processedParams <- processParameterBatch(rawParams)
    yield ProcessedParameters(processedParams, validatedParams)

  case class ProcessedParameters(raw: List[Float], validated: List[Float])

  private def processParameterBatch(parameters: List[Float]): Try[List[Float]] =
    Try(parameters.map(_ * 100.0f).filter(_ > 10.0f))

  /**
   * Composing multiple Either operations
   * Chains multiple operations with detailed error handling
   */
  def loadAndValidateParameters(filePath: String): Either[ProcessingError, ValidatedParameters] =
    for
      rawParams <- processParameterFile(filePath)
      validatedParams <- validateParametersEither(rawParams)
      statistics <- calculateParameterStatistics(validatedParams)
    yield ValidatedParameters(validatedParams, statistics)

  case class ValidatedParameters(parameters: List[Float], statistics: ParameterStatistics)
  case class ParameterStatistics(mean: Float, min: Float, max: Float, count: Int)

  private def calculateParameterStatistics(parameters: List[Float]): Either[ProcessingError, ParameterStatistics] =
    if parameters.isEmpty then
      Left(ValidationError(List("Cannot calculate statistics for empty parameter list")))
    else
      val mean = parameters.sum / parameters.length
      val min = parameters.min
      val max = parameters.max
      Right(ParameterStatistics(mean, min, max, parameters.length))

  // ============================================================================
  // Functional Error Handling Patterns
  // ============================================================================

  /**
   * Functional error handling with map/flatMap
   * Uses functional composition for error handling
   */
  def functionalParameterProcessing(filePath: String): Try[List[Float]] =
    Try(Source.fromFile(filePath))
      .flatMap { source =>
        Try(source.getLines().toList)
      }
      .map(_.filter(_.trim.nonEmpty))
      .flatMap { lines =>
        Try(lines.map(_.toFloat))
      }
      .map(_.filter(param => param >= 0.0f && param <= 1.0f))
      .recover { case e: FileNotFoundException =>
        println(s"File not found, using default parameters: ${e.getMessage}")
        List.fill(5)(0.5f)
      }

  /**
   * Either with functional composition
   * Uses Either for functional error handling
   */
  def functionalEitherProcessing(filePath: String): Either[String, List[Float]] =
    for
      path <- Either.cond(
        Paths.get(filePath).toFile.exists(),
        Paths.get(filePath),
        s"File not found: $filePath"
      )
      content <- Try(java.nio.file.Files.readString(path)).toEither.left.map(_.getMessage)
      parameters <- Try(content.linesIterator.map(_.toFloat).toList).toEither.left.map(_.getMessage)
      validParams <- Either.cond(
        parameters.forall(p => p >= 0.0f && p <= 1.0f),
        parameters,
        "Parameters contain invalid values"
      )
    yield validParams

  // ============================================================================
  // Error Aggregation and Reporting
  // ============================================================================

  /**
   * Aggregate multiple errors
   * Collects all errors from multiple operations
   */
  def aggregateParameterErrors(filePaths: List[String]): (List[String], List[List[Float]]) =
    val results = filePaths.map { path =>
      loadParameterFileWithContext(path).toEither
    }

    val (errors, successes) = results.partitionMap(identity)
    (errors.map(_.getMessage), successes)

  /**
   * Detailed error reporting
   * Provides comprehensive error information
   */
  def detailedErrorReport(filePath: String): ErrorReport =
    loadParameterFileWithContext(filePath) match
      case Success(parameters) =>
        ErrorReport.Success(parameters.length, parameters.sum / parameters.length)
      case Failure(e: FileNotFoundException) =>
        ErrorReport.FileNotFound(filePath, e.getMessage)
      case Failure(e: NumberFormatException) =>
        ErrorReport.ParseError(filePath, e.getMessage)
      case Failure(e: IllegalArgumentException) =>
        ErrorReport.ValidationError(filePath, e.getMessage)
      case Failure(e) =>
        ErrorReport.UnknownError(filePath, e.getMessage)

  sealed trait ErrorReport
  object ErrorReport:
    case class Success(parameterCount: Int, averageValue: Float) extends ErrorReport
    case class FileNotFound(path: String, message: String) extends ErrorReport
    case class ParseError(path: String, message: String) extends ErrorReport
    case class ValidationError(path: String, message: String) extends ErrorReport
    case class UnknownError(path: String, message: String) extends ErrorReport

  // ============================================================================
  // Utility Methods for Error Handling
  // ============================================================================

  /**
   * Safe conversion with error handling
   * Converts values safely with detailed error information
   */
  def safeConvert[A, B](value: A, converter: A => B, errorContext: String): Either[String, B] =
    Try(converter(value)).toEither.left.map { e =>
      s"$errorContext: ${e.getMessage}"
    }

  /**
   * Retry mechanism with exponential backoff
   * Retries operations with increasing delays
   */
  def retryWithBackoff[A](operation: => A, maxRetries: Int = 3): Try[A] =
    def retry(attempt: Int): Try[A] =
      Try(operation).recoverWith { case e if attempt < maxRetries =>
        val delay = math.pow(2, attempt).toLong * 1000 // Exponential backoff
        Thread.sleep(delay)
        println(s"Retry attempt ${attempt + 1} after ${delay}ms delay")
        retry(attempt + 1)
      }

    retry(0)

  /**
   * Timeout wrapper for operations
   * Adds timeout to potentially long-running operations
   */
  def withTimeout[A](operation: => A, timeoutMs: Long): Try[A] =
    Try {
      val future = scala.concurrent.Future(operation)(scala.concurrent.ExecutionContext.global)
      scala.concurrent.Await.result(future, scala.concurrent.duration.Duration(timeoutMs, "ms"))
    }

  // ============================================================================
  // Example Usage and Best Practices
  // ============================================================================

  /**
   * Example: Comprehensive parameter loading with error handling
   */
  def loadParametersComprehensive(filePath: String): Either[ProcessingError, ValidatedParameters] =
    loadAndValidateParameters(filePath)
      .orElse {
        println(s"Primary loading failed, trying fallback")
        loadAndValidateParameters(filePath + ".backup")
      }
      .orElse {
        println(s"All loading attempts failed, using defaults")
        Right(ValidatedParameters(
          List.fill(10)(0.5f),
          ParameterStatistics(0.5f, 0.5f, 0.5f, 10)
        ))
      }

  /**
   * Example: Error handling in parameter processing pipeline
   */
  def parameterProcessingPipeline(filePaths: List[String]): ProcessingResult =
    val results = filePaths.map { path =>
      val report = detailedErrorReport(path)
      (path, report)
    }

    val successful = results.collect { case (path, ErrorReport.Success(count, avg)) =>
      (path, count, avg)
    }

    val errors = results.collect { case (path, error: ErrorReport) if !error.isInstanceOf[ErrorReport.Success] =>
      (path, error)
    }

    ProcessingResult(successful, errors)

  case class ProcessingResult(
    successful: List[(String, Int, Float)],
    errors: List[(String, ErrorReport)]
  )

end EnhancedErrorHandling
