package moe.brianhsu.live2d.demo.swt.theme

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.{Color, Font}
import org.eclipse.swt.widgets.{Composite, Control}

/**
 * Utility to apply IntelliJ IDEA "idea_dark" style to SWT controls.
 */
object IdeaDarkThemeManager {
  // Primary colors used in IntelliJ IDEA's dark theme
  private val backgroundColor = new Color(null, 43, 43, 43)    // #2b2b2b
  private val foregroundColor = new Color(null, 169, 183, 198) // #a9b7c6
  private val font = new Font(null, "Consolas", 11, SWT.NORMAL)

  /** Apply theme recursively to the given control and all its children. */
  def applyTheme(control: Control): Unit = {
    control.setBackground(backgroundColor)
    control.setForeground(foregroundColor)
    control.setFont(font)

    control match {
      case composite: Composite =>
        composite.getChildren.foreach(applyTheme)
      case _ => // Ignore non-composite controls
    }
  }

  /** Dispose resources created by this theme manager. */
  def disposeResources(): Unit = {
    backgroundColor.dispose()
    foregroundColor.dispose()
    font.dispose()
  }
}

