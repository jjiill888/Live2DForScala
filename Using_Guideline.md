# Using Guideline

If you only want to use the program, you need to do the following:


---

##  Step 1. Install OpenJDK 21

### Linux

Install OpenJDK 21 using your distribution's package manager:

- **Ubuntu**:
  ```bash
  sudo apt-get install openjdk-21-jre
  ```

- **Arch Linux**:
  ```bash
  sudo pacman -S jre21-openjdk
  ```

- **Gentoo**:
  ```bash
  sudo emerge -pv virtual/jre:21
  ```

### Windows

Download and install `microsoft-jdk-21.X.Y-windows-x64.msi` from [Microsoft Build of OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download).

 ###  MacOS
      Install Homebrew
      Run brew install openjdk@21 to install
          


---

##  Step 2. Download Compressed Files

Go to the **Releases** section on GitHub and download the appropriate version:

- **Linux (X11)**: `Live2DForScala-SWT-Linux-X.Y.Z.tar.gz`
- **Linux (Wayland)**:
  -  `Live2DForScala-SWT-Linux-X.Y.Z.tar.gz` 
  -  or `Live2DForScala-Swing-X.Y.Z.zip`
- **Windows**:
  - `Live2DForScala-Swing-X.Y.Z.zip`
  - or `Live2DForScala-SWT-Windows-X.Y.Z.zip`
-  **Mac**: `Live2DForScala-Swing-X.Y.Z.zip`

Extract the downloaded archive.

---

##  Step 3. Run the Demo Application

- **Windows**: Double-click `start.bat`
- **Linux**: Double-click `start.desktop`(x11)
- If you using Wayland,please edit 'start.desktop's context :`GDK_BACKEND=x11 java -jar Live2DForScala-SWT-Linux-2.0.0-SNAPSHOT.jar`

-  **Mac**: `java -jar Live2DForScala-Swing-X.Y.Z-SNAPSHOT.jar`

---

##  Step 4. Load a Live2D Model and Have Fun!

- Click the `Load Avatar` button (top-left) to load a model.
  - Select a folder that contains a valid `.moc3` file and its assets.
- Use the **left control panel** to manage:
  - Effects
  - Motions
  - Expressions
- **Mouse Controls**:
  - Right-click and drag to move the avatar
  - Scroll wheel to zoom in/out
  - **Left double-click** to toggle control panel visibility
- **Keyboard Controls**:
  - **ESC key** to toggle control panel visibility
- Click `Default Avatar` to auto-load from the `def_avatar/` folder next to the JAR
- The application saves the last loaded model path to `last_avatar`
  - It auto-loads this model on next launch
  - Falls back to `def_avatar` if unavailable
- Enable `Auto Start`, `Simulate Eye Gaze` and `Disable Eye Blink` in the tracking panel to save preferences to `auto_start`
- Toggle `Transparent Background` in the toolbar for alpha channel output (e.g. for OBS streaming)

---

##  Step 5. How to run two Avatar on the same computer
You can run two face tracking instances simultaneously on one computer by setting different ports and assigning the correct camera for each in the Face Tracking panel.

---

##  New Features in This Fork

- **Expression Shortcut Keys (1–9)** – Instantly switch facial expressions
- **Default Avatar** – Loads fallback model automatically
- **Full-Body Sway from Face Tracking** – More immersive avatar movement
- **OBS-Friendly Transparency** – For streaming with alpha channel
- **Simulated Eye Gaze** – Natural idle gaze movement
- **XWayland support** SWT version can working on XWayland
- **UI Toggle Controls** – ESC key or left double-click to hide/show control panels
- Run two Avatar on the same computer
---

Happy streaming!
