# Using Guideline

If you only want to use the program, you need to do the following:

> ⚠️ Note: macOS version is currently **not released**.

---

##  Step 1. Install OpenJDK 17

### Linux

Install OpenJDK 17 using your distribution’s package manager:

- **Ubuntu**:
  ```bash
  sudo apt-get install openjdk-17-jre
  ```

- **Arch Linux**:
  ```bash
  sudo pacman -S jre17-openjdk
  ```

- **Gentoo**:
  ```bash
  sudo emerge -pv virtual/jre:7
  ```

### Windows

Download and install `microsoft-jdk-17.X.Y-windows-x64.msi` from [Microsoft Build of OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download).

---

##  Step 2. Download Compressed Files

Go to the **Releases** section on GitHub and download the appropriate version:

- **Linux (X11)**: `Live2DForScala-SWT-Linux-X.Y.Z.tar.gz`
- **Linux (Wayland)**: `Live2DForScala-Swing-X.Y.Z.zip`
- **Windows**:
  - `Live2DForScala-Swing-X.Y.Z.zip`
  - or `Live2DForScala-SWT-Windows-X.Y.Z.zip`

Extract the downloaded archive.

---

##  Step 3. Run the Demo Application

- **Windows**: Double-click `start.bat`
- **Linux**: Double-click `start.desktop`

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
- Click `Default Avatar` to auto-load from the `def_avatar/` folder next to the JAR
- The application saves the last loaded model path to `last_avatar`
  - It auto-loads this model on next launch
  - Falls back to `def_avatar` if unavailable
- Enable `Auto Start` and `Simulate Eye Gaze` in the tracking panel to save preferences to `auto_start`
- Toggle `Transparent Background` in the toolbar for alpha channel output (e.g. for OBS streaming)

---

##  New Features in This Fork

- **Expression Shortcut Keys (1–9)** – Instantly switch facial expressions
- **Default Avatar** – Loads fallback model automatically
- **Full-Body Sway from Face Tracking** – More immersive avatar movement
- **OBS-Friendly Transparency** – For streaming with alpha channel
- **Simulated Eye Gaze** – Natural idle gaze movement

---

Happy streaming!