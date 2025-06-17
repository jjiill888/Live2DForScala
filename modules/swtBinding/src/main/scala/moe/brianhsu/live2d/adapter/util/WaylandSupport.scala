package moe.brianhsu.live2d.adapter.util

/** Helper to configure SWT for running under Wayland. */
object WaylandSupport {
  /**
    * Setup system properties that allow SWT to operate on Wayland.
    * If the application is already running in a Wayland environment
    * and no backend has been specified, this enables the Wayland backend.
    */
  def setup(): Unit = {
    val env = sys.env
    val isWayland = env.contains("WAYLAND_DISPLAY") || env.get("XDG_SESSION_TYPE").exists(_.equalsIgnoreCase("wayland"))
    val backendSet = env.get("GDK_BACKEND").exists(_.toLowerCase.contains("wayland")) ||
      System.getProperty("GDK_BACKEND") != null
    if (isWayland && !backendSet) {
      System.setProperty("GDK_BACKEND", "wayland")
    }
    if (isWayland && System.getProperty("SWT_GTK3") == null) {
      System.setProperty("SWT_GTK3", "1")
    }
  }
}


