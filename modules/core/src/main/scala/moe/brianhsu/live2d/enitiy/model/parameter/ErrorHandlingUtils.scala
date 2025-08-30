package moe.brianhsu.live2d.enitiy.model.parameter

import scala.util.{Try, Success, Failure, Either, Left, Right}
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.Duration
import java.io.{FileNotFoundException, IOException}
import java.nio.file.{Path, Paths}

/**
 * Error Handling Utilities for Live2D Parameter Processing
 * 
 * Provides common error handling patterns and utilities for better error management
 * in Live2D parameter processing.
 */
object ErrorHandlingUtils:

  // ============================================================================
  // Try Utilities
  // ============================================================================

  /**
   * Enhanced Try with better error context
   */
  def tryWithContext[A](operation: => A, context: String): Try[A] =
    Try(operation).recoverWith { case e =>
      Failure(new RuntimeException(s"$context: ${e.getMessage}", e))
    }

  /**
   * Try with timeout
   */
  def tryWithTimeout[A](operation: => A, timeoutMs: Long): Try[A] =
    Try {
      val future = Future(operation)(ExecutionContext.global)
      scala.concurrent.Await.result(future, Duration(timeoutMs, "ms"))
    }

  /**
   * Try with retry mechanism
   */
  def tryWithRetry[A](operation: => A, maxRetries: Int = 3, delayMs: Long = 1000): Try[A] =
    def retry(attempt: Int): Try[A] =
      Try(operation).recoverWith { case e if attempt < maxRetries =>
        Thread.sleep(delayMs)
        println(s"Retry attempt ${attempt + 1} after ${delayMs}ms delay")
        retry(attempt + 1)
      }

    retry(0)

  /**
   * Try with exponential backoff
   */
  def tryWithExponentialBackoff[A](operation: => A, maxRetries: Int = 3, baseDelayMs: Long = 1000): Try[A] =
    def retry(attempt: Int): Try[A] =
      Try(operation).recoverWith { case e if attempt < maxRetries =>
        val delay = baseDelayMs * math.pow(2, attempt).toLong
        Thread.sleep(delay)
        println(s"Retry attempt ${attempt + 1} after ${delay}ms delay")
        retry(attempt + 1)
      }

    retry(0)

  // ============================================================================
  // Either Utilities
  // ============================================================================

  /**
   * Safe conversion with Either
   */
  def safeConvert[A, B](value: A, converter: A => B, errorContext: String): Either[String, B] =
    Try(converter(value)).toEither.left.map { e =>
      s"$errorContext: ${e.getMessage}"
    }

  /**
   * Either with fallback
   */
  def eitherWithFallback[A, B](operation: => Either[A, B], fallback: => Either[A, B]): Either[A, B] =
    operation.orElse(fallback)

  /**
   * Either with multiple fallbacks
   */
  def eitherWithFallbacks[A, B](operation: => Either[A, B], fallbacks: List[() => Either[A, B]]): Either[A, B] =
    fallbacks.foldLeft(operation) { (current, fallback) =>
      current.orElse(fallback())
    }

  // ============================================================================
  // File Operation Error Handling
  // ============================================================================

  /**
   * Safe file reading with detailed error handling
   */
  def safeReadFile(filePath: String): Either[String, String] =
    for
      path <- validateFilePath(filePath)
      content <- readFileContent(path)
    yield content

  private def validateFilePath(filePath: String): Either[String, Path] =
    val path = Paths.get(filePath)
    if path.toFile.exists() then
      if path.toFile.canRead() then
        Right(path)
      else
        Left(s"Permission denied: $filePath")
    else
      Left(s"File not found: $filePath")

  private def readFileContent(path: Path): Either[String, String] =
    Try(java.nio.file.Files.readString(path)).toEither.left.map { e =>
      s"Failed to read file ${path}: ${e.getMessage}"
    }

  /**
   * Safe file writing with error handling
   */
  def safeWriteFile(filePath: String, content: String): Either[String, Unit] =
    Try {
      val path = Paths.get(filePath)
      java.nio.file.Files.writeString(path, content)
      ()
    }.toEither.left.map { e =>
      s"Failed to write file $filePath: ${e.getMessage}"
    }

  // ============================================================================
  // Parameter Validation Error Handling
  // ============================================================================

  /**
   * Validate parameters with detailed error reporting
   */
  def validateParametersWithErrors(parameters: List[Float]): Either[List[String], List[Float]] =
    val errors = parameters.zipWithIndex.flatMap { case (param, index) =>
      if param < 0.0f then
        Some(s"Parameter at index $index ($param) is negative")
      else if param > 1.0f then
        Some(s"Parameter at index $index ($param) exceeds maximum value 1.0")
      else if param.isNaN then
        Some(s"Parameter at index $index ($param) is NaN")
      else if param.isInfinite then
        Some(s"Parameter at index $index ($param) is infinite")
      else
        None
    }

    if errors.isEmpty then
      Right(parameters)
    else
      Left(errors)

  /**
   * Validate parameter file with comprehensive error checking
   */
  def validateParameterFile(filePath: String): Either[List[String], List[Float]] =
    for
      content <- safeReadFile(filePath).left.map(e => List(e))
      lines <- parseFileLines(content).left.map(e => List(e))
      parameters <- parseParameters(lines).left.map(e => List(e))
      validatedParams <- validateParametersWithErrors(parameters)
    yield validatedParams

  private def parseFileLines(content: String): Either[String, List[String]] =
    val lines = content.linesIterator.map(_.trim).filter(_.nonEmpty).toList
    if lines.isEmpty then
      Left("File is empty or contains no valid lines")
    else
      Right(lines)

  private def parseParameters(lines: List[String]): Either[String, List[Float]] =
    Try(lines.map(_.toFloat)).toEither.left.map { e =>
      s"Failed to parse parameters: ${e.getMessage}"
    }

  // ============================================================================
  // Error Recovery Strategies
  // ============================================================================

  /**
   * Recovery strategy with multiple fallbacks
   */
  def recoverWithFallbacks[A](operation: => Try[A], fallbacks: List[() => Try[A]]): Try[A] =
    fallbacks.foldLeft(operation) { (current, fallback) =>
      current.recoverWith { case _ => fallback() }
    }

  /**
   * Recovery with default value
   */
  def recoverWithDefault[A](operation: => Try[A], default: A): Try[A] =
    operation.recover { case _ => default }

  /**
   * Recovery with error logging
   */
  def recoverWithLogging[A](operation: => Try[A], errorMessage: String): Try[A] =
    operation.recoverWith { case e =>
      println(s"$errorMessage: ${e.getMessage}")
      Failure(e)
    }

  // ============================================================================
  // Error Aggregation
  // ============================================================================

  /**
   * Aggregate errors from multiple operations
   */
  def aggregateErrors[A, B](operations: List[() => Try[A]]): (List[Throwable], List[A]) =
    val results = operations.map(_())
    val (failures, successes) = results.partition(_.isFailure)
    (failures.map(_.failed.get), successes.map(_.get))

  /**
   * Aggregate Either results
   */
  def aggregateEitherResults[A, B](operations: List[() => Either[A, B]]): (List[A], List[B]) =
    val results = operations.map(_())
    results.partitionMap(identity)

  // ============================================================================
  // Error Reporting
  // ============================================================================

  /**
   * Generate detailed error report
   */
  def generateErrorReport[A](operation: => Try[A], operationName: String): ErrorReport =
    operation match
      case Success(result) =>
        ErrorReport.Success(operationName, result.toString)
      case Failure(e: FileNotFoundException) =>
        ErrorReport.FileNotFound(operationName, e.getMessage)
      case Failure(e: IOException) =>
        ErrorReport.IOError(operationName, e.getMessage)
      case Failure(e: IllegalArgumentException) =>
        ErrorReport.ValidationError(operationName, e.getMessage)
      case Failure(e) =>
        ErrorReport.UnknownError(operationName, e.getMessage)

  sealed trait ErrorReport
  object ErrorReport:
    case class Success(operation: String, result: String) extends ErrorReport
    case class FileNotFound(operation: String, message: String) extends ErrorReport
    case class IOError(operation: String, message: String) extends ErrorReport
    case class ValidationError(operation: String, message: String) extends ErrorReport
    case class UnknownError(operation: String, message: String) extends ErrorReport

  /**
   * Print error report
   */
  def printErrorReport(report: ErrorReport): Unit =
    report match
      case ErrorReport.Success(operation, result) =>
        println(s"✅ $operation: $result")
      case ErrorReport.FileNotFound(operation, message) =>
        println(s"❌ $operation - File not found: $message")
      case ErrorReport.IOError(operation, message) =>
        println(s"❌ $operation - IO error: $message")
      case ErrorReport.ValidationError(operation, message) =>
        println(s"❌ $operation - Validation error: $message")
      case ErrorReport.UnknownError(operation, message) =>
        println(s"❌ $operation - Unknown error: $message")

  // ============================================================================
  // Utility Methods
  // ============================================================================

  /**
   * Convert Try to Either
   */
  def tryToEither[A](tryValue: Try[A]): Either[Throwable, A] =
    tryValue.toEither

  /**
   * Convert Either to Try
   */
  def eitherToTry[A](eitherValue: Either[Throwable, A]): Try[A] =
    eitherValue.toTry

  /**
   * Safe execution with error handling
   */
  def safeExecute[A](operation: => A, errorHandler: Throwable => Unit): Option[A] =
    Try(operation).toOption

  /**
   * Execute with error recovery
   */
  def executeWithRecovery[A](operation: => A, recovery: Throwable => A): A =
    Try(operation).recover { case e => recovery(e) }.get

end ErrorHandlingUtils
