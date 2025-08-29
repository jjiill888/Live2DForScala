#  Developing Guideline

This guide describes how to build, test, and contribute to the Live2D For Scala forked project.

---

##  Prerequisites

- **Java Development Kit (JDK) 21**
  - Windows: [Microsoft Build of OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)
  - Linux: Install via your distro's package manager
  - macOS: `brew install openjdk@21`

- **SBT (Scala Build Tool)**
  - Installation guide: [https://www.scala-sbt.org/download.html](https://www.scala-sbt.org/download.html)

---

##  Build & Test Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/jjiill888/Live2DForScala.git
cd Live2DForScala
```

### 2. Run SBT Console

```bash
sbt
```

### 3. Compile the Code

```sbt
compile
```

### 4. Run Unit Tests

```sbt
test
```

> ⚠ On macOS, SWT + LWJGL-based tests may fail due to platform limitations.

---

##  Running Demo Applications

Inside the SBT console, use:

```sbt
exampleSwing/run         # Run Swing version
exampleSWTLinux/run      # Run SWT version on Linux
exampleSWTWin/run        # Run SWT version on Windows
```

---

##  Packaging with Assembly

To generate executable JAR files:

```sbt
exampleSwing/assembly
exampleSWTLinux/assembly
exampleSWTWin/assembly
```

---

##  Creating a Release Package

To bundle the app with dependencies and OpenSeeFace face tracking engine:

```sbt
releaseswing     # Swing version
releaselinux     # SWT Linux version
releasewin       # SWT Windows version
```

You may ignore errors related to existing directories on reruns.

---

##  Project Structure Overview

```
modules/
├── core              # Core rendering and Live2D logic
├── joglBinding       # JOGL OpenGL binding
├── lwjglBinding      # LWJGL binding
├── swtBinding        # SWT integration
└── examples/
    ├── swing         # Swing-based demo
    ├── swt           # SWT-based demo
    └── swt-*-bundle  # Platform-specific bundles
```

---

##  Notes

- Make sure your JDK version is 21 (some modules may not run correctly on older versions).
- First SBT run may take a while as it downloads dependencies.

For more details, see: [`Old_README.md`](./Old_README.md)