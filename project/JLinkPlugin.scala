import sbt._
import Keys._
import java.io.File
import sys.process._

object JLinkPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val jlinkModules = settingKey[Seq[String]]("Java modules to include in custom JRE")
    val jlinkOptions = settingKey[Seq[String]]("Additional jlink options")
    val jlinkImageName = settingKey[String]("Name of the custom JRE image")
    val jlink = taskKey[Unit]("Create custom JRE using jlink")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    jlinkModules := Seq(
      "java.base",
      "java.desktop", 
      "java.logging",
      "java.management",
      "java.naming",
      "java.security.jgss",
      "java.sql",
      "jdk.unsupported",
      "jdk.incubator.vector"
    ),
    jlinkOptions := Seq(
      "--no-header-files",
      "--no-man-pages",
      "--strip-debug",
      "--compress=2"
    ),
    jlinkImageName := "jre-custom",
    
    jlink := {
      val log = streams.value.log
      val modules = jlinkModules.value
      val options = jlinkOptions.value
      val imageName = jlinkImageName.value
      
      val targetDir = (Compile / target).value
      val jreDir = targetDir / "jlink" / imageName
      
      // Remove existing directory if it exists
      if (jreDir.exists()) {
        log.info(s"Removing existing JRE directory: ${jreDir.getAbsolutePath}")
        IO.delete(jreDir)
      }
      
      jreDir.getParentFile.mkdirs()
      
      val javaHome = System.getProperty("java.home")
      val jmodsPath = new File(javaHome, "jmods").getAbsolutePath
      
      val cmd = Seq("jlink") ++
        Seq("--module-path", jmodsPath) ++
        Seq("--add-modules", modules.mkString(",")) ++
        Seq("--output", jreDir.getAbsolutePath) ++
        options
      
      log.info(s"Running jlink: ${cmd.mkString(" ")}")
      
      val result = cmd.!
      
      if (result == 0) {
        log.success(s"Custom JRE created successfully at: ${jreDir.getAbsolutePath}")
      } else {
        throw new RuntimeException(s"Failed to create custom JRE, exit code: $result")
      }
    }
  )
}
