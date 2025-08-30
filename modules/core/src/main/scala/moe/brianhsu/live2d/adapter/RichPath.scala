package moe.brianhsu.live2d.adapter

import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path}
import scala.util.Try

/**
 * Extension methods for Java NIO Path to provide enhanced functionality.
 *
 * @example
 *  {{{
 *    import java.nio.file.Paths
 *    import moe.brianhsu.live2d.adapter.RichPath._
 *
 *    val path = Paths.get("/usr/share/doc")
 *    val isReadableFile = path.isReadableFile
 *  }}}
 */
object RichPath:
  // Extension methods for enhanced Path functionality
  extension (path: Path)
    /**
     * Is this path a readable file?
     *
     * @return `true` if it's a readable file, `false` otherwise.
     */
    def isReadableFile: Boolean =
      Files.exists(path) &&
        Files.isRegularFile(path) &&
        Files.isReadable(path)

    /**
     * Read the content from the path into String.
     *
     * @param charset The encoding of file represented by this path.
     * @return A [[scala.util.Success]]`[String]` containing the file content, or a [[scala.util.Failure]]`[Throwable]` if something goes wrong.
     */
    def readToString(charset: Charset = StandardCharsets.UTF_8): Try[String] = Try {
      new String(Files.readAllBytes(path), charset)
    }
    
    /**
     * Check if the path exists and is a directory.
     *
     * @return `true` if it's an existing directory, `false` otherwise.
     */
    def isExistingDirectory: Boolean =
      Files.exists(path) && Files.isDirectory(path)
    
    /**
     * Get the file size in bytes.
     *
     * @return The file size in bytes, or 0 if the file doesn't exist.
     */
    def fileSize: Long =
      if Files.exists(path) then Files.size(path) else 0L
    
    /**
     * Get the file extension.
     *
     * @return The file extension (without the dot), or empty string if no extension.
     */
    def extension: String =
      val fileName = path.getFileName.toString
      val lastDotIndex = fileName.lastIndexOf('.')
      if lastDotIndex > 0 then fileName.substring(lastDotIndex + 1) else ""
    
    /**
     * Check if the file has a specific extension.
     *
     * @param ext The extension to check (without the dot).
     * @return `true` if the file has the specified extension, `false` otherwise.
     */
    def hasExtension(ext: String): Boolean =
      extension.equalsIgnoreCase(ext)
