package moe.brianhsu.live2d.enitiy.opengl

import java.nio.ByteBuffer
import scala.util.Try

/**
 * Enhanced OpenGL Binding with Scala 3 extension methods and given/using patterns.
 * This demonstrates how to create a more ergonomic OpenGL API using Scala 3 features.
 */
object EnhancedOpenGLBinding:
  
  // Extension methods for enhanced OpenGL operations
  extension (gl: OpenGLBinding)
    /**
     * Safe texture creation with error handling
     */
    def createTextureSafely(): Try[Int] = Try {
      val textureIds = new Array[Int](1)
      gl.glGenTextures(1, textureIds)
      textureIds(0)
    }
    
    /**
     * Bind texture with type safety
     */
    def bindTexture2D(textureId: Int): Unit =
      gl.glBindTexture(gl.constants.GL_TEXTURE_2D, textureId)
    
    /**
     * Set texture parameters with common defaults
     */
    def setTextureParameters(
      minFilter: Int = gl.constants.GL_LINEAR,
      magFilter: Int = gl.constants.GL_LINEAR,
      wrapS: Int = gl.constants.GL_CLAMP_TO_EDGE,
      wrapT: Int = gl.constants.GL_CLAMP_TO_EDGE
    ): Unit =
      gl.glTexParameteri(gl.constants.GL_TEXTURE_2D, gl.constants.GL_TEXTURE_MIN_FILTER, minFilter)
      gl.glTexParameteri(gl.constants.GL_TEXTURE_2D, gl.constants.GL_TEXTURE_MAG_FILTER, magFilter)
      gl.glTexParameteri(gl.constants.GL_TEXTURE_2D, gl.constants.GL_TEXTURE_WRAP_S, wrapS)
      gl.glTexParameteri(gl.constants.GL_TEXTURE_2D, gl.constants.GL_TEXTURE_WRAP_T, wrapT)
    
    /**
     * Upload texture data with automatic mipmap generation
     */
    def uploadTextureData(
      width: Int, 
      height: Int, 
      data: ByteBuffer, 
      format: Int = gl.constants.GL_RGBA,
      internalFormat: Int = gl.constants.GL_RGBA,
      dataType: Int = gl.constants.GL_UNSIGNED_BYTE
    ): Unit =
      gl.glTexImage2D(
        gl.constants.GL_TEXTURE_2D, 0, internalFormat,
        width, height, 0, format, dataType, data
      )
      gl.glGenerateMipmap(gl.constants.GL_TEXTURE_2D)
    
    /**
     * Set viewport with bounds checking
     */
    def setViewport(x: Int, y: Int, width: Int, height: Int): Unit =
      require(width > 0 && height > 0, "Viewport dimensions must be positive")
      gl.glViewport(x, y, width, height)
    
    /**
     * Use shader program with error checking
     */
    def useProgramSafely(programId: Int): Try[Unit] = Try {
      gl.glUseProgram(programId)
    }
    
    /**
     * Set uniform integer with error checking
     */
    def setUniformInt(location: Int, value: Int): Try[Unit] = Try {
      gl.glUniform1i(location, value)
    }
    
    /**
     * Set uniform matrix with error checking
     */
    def setUniformMatrix4f(location: Int, matrix: Array[Float]): Try[Unit] = Try {
      gl.glUniformMatrix4fv(location, 1, false, matrix)
    }

  // Extension methods for texture management
  extension (textureId: Int)
    def bindTexture(using gl: OpenGLBinding): Unit = gl.bindTexture2D(textureId)
    
    def deleteTexture(using gl: OpenGLBinding): Unit = 
      gl.glDeleteTextures(1, Array(textureId))
    
    def setTextureParameters(
      minFilter: Int,
      magFilter: Int
    )(using gl: OpenGLBinding): Unit =
      gl.setTextureParameters(minFilter, magFilter)

  // Extension methods for shader programs
  extension (programId: Int)
    def useProgram(using gl: OpenGLBinding): Try[Unit] = gl.useProgramSafely(programId)
    
    def deleteProgram(using gl: OpenGLBinding): Unit = gl.glDeleteProgram(programId)
    
    def getUniformLocation(name: String)(using gl: OpenGLBinding): Int = 
      gl.glGetUniformLocation(programId, name)

  // Extension methods for uniform locations
  extension (location: Int)
    def setInt(value: Int)(using gl: OpenGLBinding): Try[Unit] = 
      gl.setUniformInt(location, value)
    
    def setMatrix4f(matrix: Array[Float])(using gl: OpenGLBinding): Try[Unit] = 
      gl.setUniformMatrix4f(location, matrix)

  // Utility functions using given/using
  def createTextureWithData(
    width: Int, 
    height: Int, 
    data: ByteBuffer
  )(using gl: OpenGLBinding): Try[Int] = for
    textureId <- gl.createTextureSafely()
    _ = textureId.bindTexture
    _ = textureId.setTextureParameters(gl.constants.GL_LINEAR, gl.constants.GL_LINEAR)
    _ = gl.uploadTextureData(width, height, data)
  yield textureId

  def createAndUseProgram(
    vertexShader: Int, 
    fragmentShader: Int
  )(using gl: OpenGLBinding): Try[Int] = Try {
    val programId = gl.glCreateProgram()
    gl.glAttachShader(programId, vertexShader)
    gl.glAttachShader(programId, fragmentShader)
    gl.glLinkProgram(programId)
    programId
  }
