package moe.brianhsu.live2d.enitiy.model.parameter

import scala.util.{Try, Success, Failure}

/**
 * Enhanced Control Structures using Scala 3 syntax improvements.
 * This demonstrates how to use new control structure features for better code organization.
 */
object EnhancedControlStructures:
  
  // 1. Enhanced for expressions with new syntax
  def processParameters(parameters: List[Float]): List[Float] =
    for
      param <- parameters
      if param >= 0.0f && param <= 1.0f
      normalized = param * 100.0f
      if normalized > 10.0f
    yield normalized
  
  // 2. Enhanced for expressions with multiple generators
  def crossProduct[A, B](listA: List[A], listB: List[B]): List[(A, B)] =
    for
      a <- listA
      b <- listB
    yield (a, b)
  
  // 3. Enhanced for expressions with pattern matching
  def processOptions(options: List[Option[String]]): List[String] =
    for
      case Some(value) <- options
      if value.nonEmpty
    yield value.toUpperCase
  
  // 4. Enhanced if expressions
  def conditionalProcessing(value: Float): String =
    if value < 0.0f then "negative"
    else if value == 0.0f then "zero"
    else if value < 1.0f then "small"
    else "large"
  
  // 5. Enhanced match expressions
  def processParameterType(paramType: String): String =
    paramType match
      case "angle" => "Angle parameter"
      case "scale" => "Scale parameter"
      case "position" => "Position parameter"
      case _ => "Unknown parameter type"
  
  // 6. Enhanced match expressions with guards
  def processValue(value: Float): String =
    value match
      case v if v < 0.0f => "Negative value"
      case v if v == 0.0f => "Zero value"
      case v if v < 0.5f => "Small positive value"
      case v if v < 1.0f => "Medium positive value"
      case _ => "Large positive value"
  
  // 7. Enhanced try-catch expressions
  def safeDivision(a: Float, b: Float): Try[Float] =
    Try(a / b).recoverWith {
      case _: ArithmeticException => Failure(new IllegalArgumentException("Division by zero"))
      case e: Exception => Failure(new RuntimeException(s"Unexpected error: ${e.getMessage}"))
    }
  
  // 8. Enhanced while loops with new syntax
  def findFirstValidIndex(values: Array[Float]): Int =
    var index = 0
    while index < values.length && values(index) < 0.0f do
      index += 1
    index
  
  // 9. Enhanced while loops with collection
  def collectValidValues(values: Array[Float]): List[Float] =
    var result = List.empty[Float]
    var index = 0
    while index < values.length do
      if values(index) >= 0.0f then
        result = values(index) :: result
      index += 1
    result.reverse
  
  // 10. Enhanced pattern matching with custom extractors
  case class ParameterRange(min: Float, max: Float)
  
  object ParameterRange:
    def unapply(value: Float): Option[ParameterRange] =
      if value >= 0.0f && value <= 1.0f then
        Some(ParameterRange(0.0f, 1.0f))
      else
        None
  
  def analyzeParameter(value: Float): String =
    value match
      case ParameterRange(min, max) => s"Parameter in range [$min, $max]"
      case v if v < 0.0f => "Parameter below range"
      case v if v > 1.0f => "Parameter above range"
      case _ => "Invalid parameter"
  
  // 11. Enhanced for expressions with yield
  def generateParameterSequence(count: Int): List[Float] =
    (for i <- 0 until count yield i.toFloat / count.toFloat).toList
  
  // 12. Enhanced for expressions with multiple conditions
  def filterAndTransform(values: List[Float]): List[Float] =
    for
      value <- values
      if value >= 0.0f
      if value <= 1.0f
      transformed = value * 2.0f
      if transformed <= 1.5f
    yield transformed
  
  // 13. Enhanced match expressions with nested patterns
  def processComplexValue(value: Any): String =
    value match
      case (x: Float, y: Float) if x >= 0.0f && y >= 0.0f => "Valid coordinate pair"
      case (x: Float, y: Float) => "Invalid coordinate pair"
      case list: List[_] if list.nonEmpty => "Non-empty list"
      case list: List[_] => "Empty list"
      case str: String if str.nonEmpty => "Non-empty string"
      case str: String => "Empty string"
      case _ => "Unknown type"
  
  // 14. Enhanced control flow with early returns
  def validateParameters(parameters: Map[String, Float]): Try[Map[String, Float]] =
    parameters.get("angle") match
      case Some(param) if param >= -180.0f && param <= 180.0f => Success(parameters)
      case _ => Failure(new IllegalArgumentException("Invalid angle parameter"))
  
  // 15. Enhanced for expressions with custom operations
  def processParameterBatch(parameters: List[Float]): List[Float] =
    for
      param <- parameters
      normalized = if param < 0.0f then 0.0f else if param > 1.0f then 1.0f else param
      scaled = normalized * 100.0f
      rounded = (scaled * 10).round / 10.0f
    yield rounded
  
  // 16. Enhanced pattern matching with type patterns
  def processValueByType(value: Any): String =
    value match
      case v: Float => s"Float value: $v"
      case v: Int => s"Int value: $v"
      case v: String => s"String value: $v"
      case v: List[_] => s"List with ${v.length} elements"
      case _ => "Unknown type"
  
  // 17. Enhanced for expressions with error handling
  def safeProcessParameters(parameters: List[String]): List[Try[Float]] =
    for param <- parameters yield
      Try(param.toFloat).recoverWith {
        case _: NumberFormatException => Failure(new IllegalArgumentException(s"Invalid number: $param"))
      }
  
  // 18. Enhanced control structures for parameter validation
  def validateParameterSet(parameters: Map[String, Float]): Either[List[String], Map[String, Float]] =
    val errors = List.newBuilder[String]
    
    for (name, value) <- parameters do
      if value < 0.0f then
        errors += s"Parameter $name cannot be negative"
      else if value > 1.0f then
        errors += s"Parameter $name cannot exceed 1.0"
    
    val errorList = errors.result()
    if errorList.isEmpty then
      Right(parameters)
    else
      Left(errorList)
  
  // 19. Enhanced for expressions with custom extractors
  object ValidParameter:
    def unapply(value: Float): Boolean = value >= 0.0f && value <= 1.0f
  
  def processValidParameters(parameters: List[Float]): List[Float] =
    for
      case ValidParameter() <- parameters
    yield parameters.head
  
  // 20. Enhanced control structures for complex logic
  def complexParameterProcessing(parameters: List[Float]): List[Float] =
    parameters match
      case Nil => Nil
      case head :: tail =>
        val processedHead = if head < 0.0f then 0.0f else if head > 1.0f then 1.0f else head
        processedHead :: complexParameterProcessing(tail)
  
  // Utility functions demonstrating the enhanced control structures
  def demonstrateControlStructures(): Unit =
    println("=== Enhanced Control Structures Demo ===")
    
    // Test for expressions
    val testParams = List(0.1f, 0.5f, 0.8f, -0.1f, 1.2f)
    val processed = processParameters(testParams)
    println(s"Processed parameters: $processed")
    
    // Test pattern matching
    val testValue = 0.7f
    val analysis = analyzeParameter(testValue)
    println(s"Parameter analysis: $analysis")
    
    // Test conditional processing
    val condition = conditionalProcessing(testValue)
    println(s"Conditional result: $condition")
    
    // Test complex processing
    val batchResult = processParameterBatch(testParams)
    println(s"Batch processing: $batchResult")
    
    println("=== End of Demo ===")
