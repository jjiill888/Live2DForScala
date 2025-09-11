name := "Example JavaFX+LWJGL"

fork := true
publishArtifact := false
assembly / assemblyJarName := s"Live2DForScala-JavaFX-${version.value}.jar"

libraryDependencies ++= Seq(
  "org.openjfx" % "javafx-controls" % "21.0.2",
  "org.openjfx" % "javafx-fxml" % "21.0.2",
  "org.openjfx" % "javafx-swing" % "21.0.2",
  "org.lwjgl" % "lwjgl-glfw" % "3.3.3"
)

// Add JavaFX platform natives  
val javafxPlatform = {
  System.getProperty("os.name").toLowerCase match {
    case osName if osName.contains("linux") => "linux"
    case osName if osName.contains("win") => "win"
    case osName if osName.contains("mac") => "mac"
    case osName => throw new RuntimeException(s"Unknown operating system $osName")
  }
}

libraryDependencies ++= Seq(
  "org.openjfx" % "javafx-controls" % "21.0.2" classifier javafxPlatform,
  "org.openjfx" % "javafx-fxml" % "21.0.2" classifier javafxPlatform,
  "org.openjfx" % "javafx-swing" % "21.0.2" classifier javafxPlatform,
  "org.openjfx" % "javafx-base" % "21.0.2" classifier javafxPlatform,
  "org.openjfx" % "javafx-graphics" % "21.0.2" classifier javafxPlatform
)

// LWJGL natives
val lwjglNatives = {
  System.getProperty("os.name").toLowerCase match {
    case osName if osName.contains("linux") => "natives-linux"
    case osName if osName.contains("win") => "natives-windows"
    case osName if osName.contains("mac") => "natives-macos"
    case osName => throw new RuntimeException(s"Unknown operating system $osName")
  }
}

libraryDependencies ++= Seq(
  "org.lwjgl" % "lwjgl-glfw" % "3.3.3" classifier lwjglNatives
)

// Disable module system for JavaFX
run / javaOptions ++= Seq(
  "-Dprism.order=sw",  // Use software rendering
  "-Djava.awt.headless=false"
)

// Add JavaFX to module path
run / javaOptions ++= {
  val jfxJars = (Compile / dependencyClasspath).value.files
    .filter(_.getName.contains("javafx"))
    .map(_.getAbsolutePath)
  
  if (jfxJars.nonEmpty) {
    val modulePath = jfxJars.mkString(java.io.File.pathSeparator)
    Seq("--module-path", modulePath, "--add-modules", "javafx.controls,javafx.fxml")
  } else {
    Seq.empty
  }
}