name := "JavaFX Binding"

libraryDependencies ++= Seq(
  "org.openjfx" % "javafx-controls" % "21.0.2",
  "org.openjfx" % "javafx-fxml" % "21.0.2",
  "org.openjfx" % "javafx-swing" % "21.0.2",
  "org.lwjgl" % "lwjgl" % "3.3.3",
  "org.lwjgl" % "lwjgl-opengl" % "3.3.3",
  "org.lwjgl" % "lwjgl-glfw" % "3.3.3"
)

// Add platform-specific LWJGL natives
val lwjglNatives = {
  System.getProperty("os.name").toLowerCase match {
    case osName if osName.contains("linux") => "natives-linux"
    case osName if osName.contains("win") => "natives-windows" 
    case osName if osName.contains("mac") => "natives-macos"
    case osName => throw new RuntimeException(s"Unknown operating system $osName")
  }
}

libraryDependencies ++= Seq(
  "org.lwjgl" % "lwjgl" % "3.3.3" classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-opengl" % "3.3.3" classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-glfw" % "3.3.3" classifier lwjglNatives
)