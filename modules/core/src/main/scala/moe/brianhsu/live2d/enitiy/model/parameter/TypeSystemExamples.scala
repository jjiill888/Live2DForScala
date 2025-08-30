package moe.brianhsu.live2d.enitiy.model.parameter

// Scala 3 Type System Optimization Examples
// This file demonstrates various Scala 3 type system features that can be used
// to enhance the Live2D parameter system

object TypeSystemExamples:
  
  // 1. Union Types for simple parameter states
  type ParameterState = "Active" | "Inactive" | "Hidden"
  
  // Type-safe constants
  val Active: "Active" = "Active"
  val Inactive: "Inactive" = "Inactive"
  val Hidden: "Hidden" = "Hidden"
  
  // 2. Intersection Types for enhanced parameter capabilities
  trait Readable:
    def read(): Float
  
  trait Writable:
    def write(value: Float): Unit
  
  trait Animated:
    def animate(): Unit
  
  trait Interactable:
    def interact(): Unit
  
  // Intersection types for different parameter capabilities
  type ReadWriteParameter = Readable & Writable
  type AnimatedParameter = Readable & Writable & Animated
  type FullParameter = Readable & Writable & Animated & Interactable
  
  // 3. Example implementation using intersection types
  class EnhancedParameter(
    private var value: Float,
    private var state: ParameterState = Active
  ) extends Readable, Writable, Animated, Interactable:
    
    def read(): Float = value
    def write(newValue: Float): Unit = value = newValue
    def animate(): Unit = println(s"Animating parameter with value: $value")
    def interact(): Unit = println(s"Parameter interacted with value: $value")
    
    def getState: ParameterState = state
    def setState(newState: ParameterState): Unit = state = newState
  
  // 4. Factory methods for creating different parameter types
  def createReadWrite(value: Float): ReadWriteParameter = 
    new EnhancedParameter(value)
  
  def createAnimated(value: Float): AnimatedParameter = 
    new EnhancedParameter(value)
  
  def createFull(value: Float): FullParameter = 
    new EnhancedParameter(value)
  
  // 5. Extension methods for enhanced functionality
  extension (state: ParameterState)
    def isActive: Boolean = state == Active
    def isInactive: Boolean = state == Inactive
    def isHidden: Boolean = state == Hidden
  
  extension (param: Readable)
    def readAsString: String = s"Value: ${param.read()}"
  
  extension (param: Writable)
    def writeAndValidate(value: Float): Boolean = 
      if value >= 0 && value <= 100 then
        param.write(value)
        true
      else false
  
  // 6. Type-safe parameter operations
  def processParameter(param: ReadWriteParameter): Unit =
    val currentValue = param.read()
    val newValue = currentValue * 1.1f
    param.write(newValue)
  
  def animateParameter(param: AnimatedParameter): Unit =
    param.animate()
    processParameter(param)
  
  def interactWithParameter(param: FullParameter): Unit =
    param.interact()
    animateParameter(param)
  
  // 7. Pattern matching with union types
  def handleParameterState(state: ParameterState): String = state match
    case Active => "Parameter is active and ready"
    case Inactive => "Parameter is inactive"
    case Hidden => "Parameter is hidden from view"
  
  // 8. Type-safe parameter collections
  type ParameterCollection[T <: Readable] = List[T]
  
  def processParameterCollection(params: ParameterCollection[ReadWriteParameter]): Unit =
    params.foreach(processParameter)
  
  // 9. Example usage
  def demonstrateTypeSystem(): Unit =
    println("=== Scala 3 Type System Optimization Examples ===")
    
    // Create different types of parameters
    val readWriteParam = createReadWrite(42.0f)
    val animatedParam = createAnimated(73.0f)
    val fullParam = createFull(99.0f)
    
    // Demonstrate type-safe operations
    println(s"ReadWrite parameter: ${readWriteParam.readAsString}")
    println(s"Animated parameter: ${animatedParam.readAsString}")
    println(s"Full parameter: ${fullParam.readAsString}")
    
    // Demonstrate state handling
    val states: List[ParameterState] = List(Active, Inactive, Hidden)
    states.foreach(state => println(handleParameterState(state)))
    
    // Demonstrate parameter processing
    processParameter(readWriteParam)
    animateParameter(animatedParam)
    interactWithParameter(fullParam)
    
    println("=== End of Examples ===")
