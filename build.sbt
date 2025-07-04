ThisBuild / organization := "moe.brianhsu.live2d"
ThisBuild / scalaVersion := "2.13.11"
ThisBuild / scalacOptions := Seq("-deprecation", "-Ywarn-unused", "-feature")
ThisBuild / publishArtifact := false
ThisBuild / Test / testOptions += Tests.Argument("-l", sys.env.get("EXCLUDE_TEST_TAG").getOrElse("noExclude"))

val swtVersion = "3.125.0"
val swtPackageName = {
  System.getProperty("os.name").toLowerCase match {
    case osName if osName.contains("linux") => "org.eclipse.swt.gtk.linux.x86_64"
    case osName if osName.contains("win") => "org.eclipse.swt.win32.win32.x86_64"
    case osName if osName.contains("mac")  => "org.eclipse.swt.cocoa.macosx.x86_64"
    case osName => throw new RuntimeException(s"Unknown operating system $osName")
  }
}

val javafxVersion = "17.0.2"

val swtFramework = "org.eclipse.platform" % swtPackageName % swtVersion exclude("org.eclipse.platform", "org.eclipse.swt")
val swtWindows = "org.eclipse.platform" % "org.eclipse.swt.win32.win32.x86_64" % swtVersion exclude("org.eclipse.platform", "org.eclipse.swt")
val swtLinux = "org.eclipse.platform" % "org.eclipse.swt.gtk.linux.x86_64" % swtVersion exclude("org.eclipse.platform", "org.eclipse.swt")

val testFramework = Seq(
  "org.scalatest" %% "scalatest" % "3.2.16" % Test,
  "org.scalamock" %% "scalamock" % "5.2.0" % Test,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % Test
)

val sharedSettings = Seq(
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports-html"),
  Compile / doc / scalacOptions ++= Seq("-private"),
  autoAPIMappings := true,
  libraryDependencies ++= testFramework,
  libraryDependencies += "com.github.sarxos" % "webcam-capture" % "0.3.12",
  coverageExcludedPackages := """moe\.brianhsu\.live2d\.demo\..*"""
)

lazy val core = (project in file("modules/core"))
  .settings(
    name := "Core",
    publishArtifact := true,
    sharedSettings
  )

lazy val joglBinding = (project in file("modules/joglBinding"))
  .dependsOn(core)
  .settings(
    name := "JOGL Binding",
    publishArtifact := true,
    sharedSettings
  )

lazy val lwjglBinding = (project in file("modules/lwjglBinding"))
  .dependsOn(core)
  .settings(
    name := "LWJGL Binding",
    publishArtifact := true,
    sharedSettings
  )

lazy val swtBinding = (project in file("modules/swtBinding"))
  .dependsOn(core)
  .settings(
    name := "SWT Binding",
    fork := true,
    publishArtifact := true,
    sharedSettings,
    libraryDependencies += swtFramework % "test,provided" 
  )

lazy val exampleBase = (project in file("modules/examples/base"))
  .dependsOn(core, joglBinding, lwjglBinding, swtBinding)
  .settings(
    name := "Examples Base",
    publishArtifact := false,
    sharedSettings
  )

lazy val exampleSwing = (project in file("modules/examples/swing"))
  .dependsOn(core, joglBinding, lwjglBinding, swtBinding, exampleBase)
  .settings(
    name := "Example Swing+JOGL",
    fork := true,
    publishArtifact := false,
    assembly / assemblyJarName := s"Live2DForScala-Swing-${version.value}.jar",
    sharedSettings
  )


lazy val exampleJavaFX = (project in file("modules/examples/javafx"))
  .dependsOn(core, joglBinding, lwjglBinding, swtBinding, exampleBase)
  .settings(
    name := "Example JavaFX",
    fork := true,
    publishArtifact := false,
    Compile / mainClass := Some("moe.brianhsu.live2d.demo.javafx.JavaFXMain"),
    assembly / assemblyJarName := s"Live2DForScala-JavaFX-${version.value}.jar",
    sharedSettings,

    libraryDependencies ++= Seq(
      "org.openjfx" % "javafx-controls" % javafxVersion classifier "linux",
      "org.openjfx" % "javafx-graphics" % javafxVersion classifier "linux",
      "org.openjfx" % "javafx-swing" % javafxVersion classifier "linux"
    ),

    run / javaOptions ++= Seq(
      "--module-path", sys.props.getOrElse("javafx.module.path", ""),
      "--add-modules", "javafx.controls,javafx.graphics,javafx.swing"
        ),

    Compile / resourceDirectories += baseDirectory.value / "src/main/resources"
  )

lazy val exampleSWT = (project in file("modules/examples/swt"))
  .dependsOn(core, joglBinding, lwjglBinding, swtBinding, exampleBase)
  .settings(
    name := "Example SWT+JWJGL",
    fork := true,
    publishArtifact := false,
    sharedSettings,
    libraryDependencies += swtFramework % "provided"
  )

lazy val exampleSWTLinux = (project in file("modules/examples/swt-linux-bundle"))
  .dependsOn(core, joglBinding, lwjglBinding, swtBinding, exampleSWT)
  .settings(
    name := "Example SWT+JWJGL Linux",
    fork := true,
    publishArtifact := false,
    Compile / mainClass := Some("moe.brianhsu.live2d.demo.swt.SWTWithLWJGLMain"),
    sharedSettings,
    assembly / assemblyJarName := s"Live2DForScala-SWT-Linux-${version.value}.jar",
    libraryDependencies += swtLinux
  )

lazy val exampleSWTWin = (project in file("modules/examples/swt-windows-bundle"))
  .dependsOn(core, joglBinding, lwjglBinding, swtBinding, exampleSWT)
  .settings(
    name := "Example SWT+JWJGL Windows",
    fork := true,
    publishArtifact := false,
    Compile / mainClass := Some("moe.brianhsu.live2d.demo.swt.SWTWithLWJGLMain"),
    sharedSettings,
    assembly / assemblyJarName := s"Live2DForScala-SWT-Windows-${version.value}.jar",
    libraryDependencies += swtWindows
  )

// win-pkg

import sbt.IO
import java.io.File
import sys.process._

lazy val createReleasePackageTaskwin = taskKey[Unit]("Creates a release package with openSeeFace and XX.JAR")

createReleasePackageTaskwin := {
  val releaseBaseDir = "release-pkg"
  val releaseSubDir = s"Live2DForScala-SWT-Windows-${version.value}"
  val releaseTarget = releaseBaseDir + File.separator + releaseSubDir


  if (!new File(releaseTarget).exists()) {
    IO.createDirectory(new File(releaseTarget))
    println(s"Directory '$releaseTarget' created.")
  } else {
    println(s"Directory '$releaseTarget' already exists.")
  }


  val sourceOpenSeeFace = new File("openSeeFace")
  if (sourceOpenSeeFace.exists()) {
    val targetOpenSeeFace = new File(releaseTarget, "openSeeFace")
    val cpCmdOpenSeeFace = Seq("cp", "-r", sourceOpenSeeFace.getAbsolutePath, targetOpenSeeFace.getAbsolutePath)
    val resultOpenSeeFace = cpCmdOpenSeeFace.!!
    if (resultOpenSeeFace != 0) {
      throw new RuntimeException(s"Failed to copy openSeeFace directory: exit code $resultOpenSeeFace")
    } else {
      println(s"Copied 'openSeeFace' directory into '$releaseTarget'")
    }
  } else {
    println("'openSeeFace' directory does not exist.")
  }
}

lazy val moveTaskwin = taskKey[Unit]("Moves XXX.JAR to the release package directory")

moveTaskwin := {

  val releaseBaseDir = "release-pkg"
  val releaseSubDir = s"Live2DForScala-SWT-Windows-${version.value}"
  val releaseTarget = releaseBaseDir + File.separator + releaseSubDir


  val extraFilePath = s"modules/examples/swt-windows-bundle/target/scala-2.13/Live2DForScala-SWT-Windows-${version.value}.jar"
  val extraFile = new File(extraFilePath)
  if (extraFile.exists()) {
    val targetExtraFile = new File(releaseTarget, s"Live2DForScala-SWT-Windows-${version.value}.jar")
    val cpCmdExtraFile = Seq("cp", extraFilePath, targetExtraFile.getAbsolutePath)
    val resultExtraFile = cpCmdExtraFile.!!
    if (resultExtraFile != 0) {
      throw new RuntimeException(s"Failed to copy extra file: exit code $resultExtraFile")
    } else {
      println(s"Copied 'Live2DForScala-SWT-Windows-${version.value}.jar' into '$releaseTarget'")
    }
  } else {
    println(s"'$extraFilePath' does not exist.")
  }
}

//write start.bat

import sbt._
import sys.process._

lazy val createStartFile = taskKey[Unit]("Create start.txt and rename to start.ps1 in release package")

createStartFile := {
  val dirPath = s"release-pkg/Live2DForScala-SWT-Windows-${version.value}"
  val filePath = dirPath + "/start.txt"
  val renamedFilePath = dirPath + "/start.bat"

  // Create the directory if it doesn't exist
  IO.createDirectory(new File(dirPath))

  // Run shell commands to create and rename the file
  Seq("sh", "-c", s"echo 'java -Xms256m -Xmx600m -XX:+UseG1GC -Dsun.java2d.opengl=true -jar Live2DForScala-SWT-Windows-${version.value}.jar' > $filePath && mv $filePath $renamedFilePath").!!
}

lazy val releasewin = taskKey[Unit]("Performs both createReleasePackageTask and moveTxtTaskwin in order")

releasewin := {
  createReleasePackageTaskwin.value
  moveTaskwin.value
  createStartFile.value
  println("Both tasks completed successfully.")
}









/// linux-pkg

import sbt.IO
import java.io.File
import sys.process._

lazy val createReleasePackageTasklinux = taskKey[Unit]("Creates a release package with openSeeFace and XX.JAR")

createReleasePackageTasklinux := {
  val releaseBaseDir = "release-pkg"
  val releaseSubDir = s"Live2DForScala-SWT-Linux-${version.value}"
  val releaseTarget = releaseBaseDir + File.separator + releaseSubDir


  if (!new File(releaseTarget).exists()) {
    IO.createDirectory(new File(releaseTarget))
    println(s"Directory '$releaseTarget' created.")
  } else {
    println(s"Directory '$releaseTarget' already exists.")
  }

  val sourceOpenSeeFace = new File("openSeeFace")
  if (sourceOpenSeeFace.exists()) {
    val targetOpenSeeFace = new File(releaseTarget, "openSeeFace")
    val cpCmdOpenSeeFace = Seq("cp", "-r", sourceOpenSeeFace.getAbsolutePath, targetOpenSeeFace.getAbsolutePath)
    val resultOpenSeeFace = cpCmdOpenSeeFace.!!
    if (resultOpenSeeFace != 0) {
      throw new RuntimeException(s"Failed to copy openSeeFace directory: exit code $resultOpenSeeFace")
    } else {
      println(s"Copied 'openSeeFace' directory into '$releaseTarget'")
    }
  } else {
    println("'openSeeFace' directory does not exist.")
  }
}

lazy val moveTasklinux = taskKey[Unit]("Moves XXX.JAR to the release package directory")

moveTasklinux := {

  val releaseBaseDir = "release-pkg"
  val releaseSubDir = s"Live2DForScala-SWT-Linux-${version.value}"
  val releaseTarget = releaseBaseDir + File.separator + releaseSubDir


  val extraFilePath = s"modules/examples/swt-linux-bundle/target/scala-2.13/Live2DForScala-SWT-Linux-${version.value}.jar"
  val extraFile = new File(extraFilePath)
  if (extraFile.exists()) {
    val targetExtraFile = new File(releaseTarget, s"Live2DForScala-SWT-Linux-${version.value}.jar")
    val cpCmdExtraFile = Seq("cp", extraFilePath, targetExtraFile.getAbsolutePath)
    val resultExtraFile = cpCmdExtraFile.!!
    if (resultExtraFile != 0) {
      throw new RuntimeException(s"Failed to copy extra file: exit code $resultExtraFile")
    } else {
      println(s"Copied 'Live2DForScala-SWT-Linux-${version.value}.jar' into '$releaseTarget'")

    }
  } else {
    println(s"'$extraFilePath' does not exist.")
  }
}
// write start.desktop

import sbt._
import java.io.{File, PrintWriter}
import scala.sys.process._

lazy val createStartScriptLinux = taskKey[Unit]("Create start.sh file with X11/Wayland detection")
lazy val createDesktopEntrylinux = taskKey[Unit]("Create start.desktop file in release package")

createStartScriptLinux := {
  val dirPath = s"release-pkg/Live2DForScala-SWT-Linux-${version.value}"
  val scriptFile = new File(dirPath, "start.sh")
  IO.createDirectory(new File(dirPath))
  val content = s"""#!/bin/sh
                   |if [ \"$$XDG_SESSION_TYPE\" = \"wayland\" ] || [ -n \"$$WAYLAND_DISPLAY\" ]; then
                   |  export GDK_BACKEND=x11
                   |fi
                   |exec java -Xms256m -Xmx600m -XX:+UseG1GC -Dsun.java2d.opengl=true -jar Live2DForScala-SWT-Linux-${version.value}.jar
                   |""".stripMargin
  IO.write(scriptFile, content)
  scriptFile.setExecutable(true)
}

createDesktopEntrylinux := {
  val dirPath = s"release-pkg/Live2DForScala-SWT-Linux-${version.value}"
  val filePath = dirPath + "/start.txt"
  val finalFilePath = dirPath + "/start.desktop"

  // Ensure the directory exists
  IO.createDirectory(new File(dirPath))

  // Write content to the file
  val content = s"""
                    [Desktop Entry]
                   |Type=Application
                   |Exec=sh start.sh
""".stripMargin

  // Using Scala's file operations to write content
  val writer = new PrintWriter(filePath)
  writer.write(content)
  writer.close()

  // Rename the file using Scala's File API
  val originalFile = new File(filePath)
  val newFile = new File(finalFilePath)
  originalFile.renameTo(newFile)
}


lazy val releaselinux = taskKey[Unit]("Performs both createReleasePackageTask and moveTask in order")

releaselinux := {
  createReleasePackageTasklinux.value
  moveTasklinux.value
  createStartScriptLinux.value
  createDesktopEntrylinux.value
  println("Both tasks completed successfully.")
}

/// swing-pkg

import sbt.IO
import java.io.File
import sys.process._

lazy val createReleasePackageTaskswing = taskKey[Unit]("Creates a release package with openSeeFace and XX.JAR")

createReleasePackageTaskswing := {
  val releaseBaseDir = "release-pkg"
  val releaseSubDir = s"Live2DForScala-Swing-${version.value}"
  val releaseTarget = releaseBaseDir + File.separator + releaseSubDir


  if (!new File(releaseTarget).exists()) {
    IO.createDirectory(new File(releaseTarget))
    println(s"Directory '$releaseTarget' created.")
  } else {
    println(s"Directory '$releaseTarget' already exists.")
  }

  val sourceOpenSeeFace = new File("openSeeFace")
  if (sourceOpenSeeFace.exists()) {
    val targetOpenSeeFace = new File(releaseTarget, "openSeeFace")
    val cpCmdOpenSeeFace = Seq("cp", "-r", sourceOpenSeeFace.getAbsolutePath, targetOpenSeeFace.getAbsolutePath)
    val resultOpenSeeFace = cpCmdOpenSeeFace.!!
    if (resultOpenSeeFace != 0) {
      throw new RuntimeException(s"Failed to copy openSeeFace directory: exit code $resultOpenSeeFace")
    } else {
      println(s"Copied 'openSeeFace' directory into '$releaseTarget'")
    }
  } else {
    println("'openSeeFace' directory does not exist.")
  }
}

lazy val moveTaskswing = taskKey[Unit]("Moves XXX.JAR to the release package directory")

moveTaskswing := {

  val releaseBaseDir = "release-pkg"
  val releaseSubDir = s"Live2DForScala-Swing-${version.value}"
  val releaseTarget = releaseBaseDir + File.separator + releaseSubDir


  val extraFilePath = s"modules/examples/swing/target/scala-2.13/Live2DForScala-Swing-${version.value}.jar"
  val extraFile = new File(extraFilePath)
  if (extraFile.exists()) {
    val targetExtraFile = new File(releaseTarget, s"Live2DForScala-Swing-${version.value}.jar")
    val cpCmdExtraFile = Seq("cp", extraFilePath, targetExtraFile.getAbsolutePath)
    val resultExtraFile = cpCmdExtraFile.!!
    if (resultExtraFile != 0) {
      throw new RuntimeException(s"Failed to copy extra file: exit code $resultExtraFile")
    } else {
      println(s"Copied 'Live2DForScala-Swing-${version.value}.jar' into '$releaseTarget'")

    }
  } else {
    println(s"'$extraFilePath' does not exist.")
  }
}

lazy val releaseswing = taskKey[Unit]("Performs both createReleasePackageTask and moveTask in order")

releaseswing := {
  createReleasePackageTaskswing.value
  moveTaskswing.value
  println("Both tasks completed successfully.")
}
