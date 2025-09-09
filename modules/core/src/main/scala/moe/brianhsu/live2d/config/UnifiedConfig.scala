package moe.brianhsu.live2d.config

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Try, Success, Failure}

/**
 * 统一的Live2D配置管理系统
 * 将所有分散的配置文件统一到一个Live2dForScalaSetting.txt文件中
 */
object UnifiedConfig {
  
  // 配置文件路径
  private val ConfigFile = new File("Live2dForScalaSetting.txt")
  
  // 配置数据类
  case class WindowSettings(
    x: Int = 100,
    y: Int = 100, 
    width: Int = 1080,
    height: Int = 720,
    maximized: Boolean = false
  )
  
  case class ModelSettings(
    lastAvatarPath: Option[String] = None,
    modelParameters: Map[String, Float] = Map.empty
  )
  
  case class AutoStartSettings(
    enabled: Boolean = false,
    eyeGaze: Boolean = false,
    pupilGaze: Boolean = true,
    disableEyeBlink: Boolean = false,
    transparentBackground: Boolean = false
  )
  
  case class LanguageSettings(
    language: String = "english"
  )
  
  case class Live2DConfig(
    window: WindowSettings = WindowSettings(),
    model: ModelSettings = ModelSettings(),
    autoStart: AutoStartSettings = AutoStartSettings(),
    language: LanguageSettings = LanguageSettings(),
    version: String = "1.0",
    lastUpdated: Long = System.currentTimeMillis()
  )
  
  // 全局配置实例（单例模式，只读取一次）
  private var _config: Option[Live2DConfig] = None
  private var _configLoaded: Boolean = false
  
  /**
   * 获取配置实例（懒加载，只读取一次）
   */
  def config: Live2DConfig = {
    if (!_configLoaded) {
      _config = Some(loadConfig())
      _configLoaded = true
    }
    _config.getOrElse(Live2DConfig())
  }
  
  /**
   * 强制重新加载配置
   */
  def reloadConfig(): Unit = {
    _config = Some(loadConfig())
    _configLoaded = true
  }
  
  /**
   * 从文件加载配置
   */
  private def loadConfig(): Live2DConfig = {
    if (!ConfigFile.exists()) {
      // 如果配置文件不存在，尝试从旧配置文件迁移
      migrateFromOldConfigs()
    }
    
    if (!ConfigFile.exists()) {
      return Live2DConfig()
    }
    
    Try {
      val source = Source.fromFile(ConfigFile, "UTF-8")
      try {
        val lines = source.getLines().toList.map(_.trim).filter(_.nonEmpty)
        parseConfigLines(lines)
      } finally {
        source.close()
      }
    }.getOrElse {
      System.err.println(s"[WARN] Cannot read config file: ${ConfigFile.getAbsolutePath}")
      Live2DConfig()
    }
  }
  
  /**
   * 解析配置行
   */
  private def parseConfigLines(lines: List[String]): Live2DConfig = {
    val settings = lines.flatMap { line =>
      if (line.contains("=")) {
        line.split("=", 2) match {
          case Array(k, v) => Some(k.trim -> v.trim)
          case _ => None
        }
      } else None
    }.toMap
    
    // 解析窗口设置
    val window = WindowSettings(
      x = settings.get("window.x").flatMap(_.toIntOption).getOrElse(100),
      y = settings.get("window.y").flatMap(_.toIntOption).getOrElse(100),
      width = settings.get("window.width").flatMap(_.toIntOption).getOrElse(1080),
      height = settings.get("window.height").flatMap(_.toIntOption).getOrElse(720),
      maximized = settings.get("window.maximized").flatMap(_.toBooleanOption).getOrElse(false)
    )
    
    // 解析模型设置
    val model = ModelSettings(
      lastAvatarPath = settings.get("model.lastAvatarPath").filter(_.nonEmpty),
      modelParameters = parseModelParameters(settings)
    )
    
    // 解析自动启动设置
    val autoStart = AutoStartSettings(
      enabled = settings.get("autoStart.enabled").flatMap(_.toBooleanOption).getOrElse(false),
      eyeGaze = settings.get("autoStart.eyeGaze").flatMap(_.toBooleanOption).getOrElse(false),
      pupilGaze = settings.get("autoStart.pupilGaze").flatMap(_.toBooleanOption).getOrElse(true),
      disableEyeBlink = settings.get("autoStart.disableEyeBlink").flatMap(_.toBooleanOption).getOrElse(false),
      transparentBackground = settings.get("autoStart.transparentBackground").flatMap(_.toBooleanOption).getOrElse(false)
    )
    
    // 解析语言设置
    val language = LanguageSettings(
      language = settings.get("language.language").getOrElse("english")
    )
    
    Live2DConfig(
      window = window,
      model = model,
      autoStart = autoStart,
      language = language,
      version = settings.get("version").getOrElse("1.0"),
      lastUpdated = settings.get("lastUpdated").flatMap(_.toLongOption).getOrElse(System.currentTimeMillis())
    )
  }
  
  /**
   * 解析模型参数
   */
  private def parseModelParameters(settings: Map[String, String]): Map[String, Float] = {
    settings.collect {
      case (key, value) if key.startsWith("model.param.") =>
        val paramName = key.substring("model.param.".length)
        value.toFloatOption.map(paramName -> _)
    }.flatten.toMap
  }
  
  /**
   * 保存配置到文件
   */
  def saveConfig(newConfig: Live2DConfig): Unit = {
    Try {
      val writer = new PrintWriter(ConfigFile, "UTF-8")
      try {
        // 写入版本信息
        writer.println(s"version=${newConfig.version}")
        writer.println(s"lastUpdated=${newConfig.lastUpdated}")
        writer.println()
        
        // 写入窗口设置
        writer.println("# Window Settings")
        writer.println(s"window.x=${newConfig.window.x}")
        writer.println(s"window.y=${newConfig.window.y}")
        writer.println(s"window.width=${newConfig.window.width}")
        writer.println(s"window.height=${newConfig.window.height}")
        writer.println(s"window.maximized=${newConfig.window.maximized}")
        writer.println()
        
        // 写入模型设置
        writer.println("# Model Settings")
        newConfig.model.lastAvatarPath.foreach { path =>
          writer.println(s"model.lastAvatarPath=$path")
        }
        newConfig.model.modelParameters.foreach { case (param, value) =>
          writer.println(s"model.param.$param=$value")
        }
        writer.println()
        
        // 写入自动启动设置
        writer.println("# Auto Start Settings")
        writer.println(s"autoStart.enabled=${newConfig.autoStart.enabled}")
        writer.println(s"autoStart.eyeGaze=${newConfig.autoStart.eyeGaze}")
        writer.println(s"autoStart.pupilGaze=${newConfig.autoStart.pupilGaze}")
        writer.println(s"autoStart.disableEyeBlink=${newConfig.autoStart.disableEyeBlink}")
        writer.println(s"autoStart.transparentBackground=${newConfig.autoStart.transparentBackground}")
        writer.println()
        
        // 写入语言设置
        writer.println("# Language Settings")
        writer.println(s"language.language=${newConfig.language.language}")
        
      } finally {
        writer.close()
      }
      
      // 更新内存中的配置
      _config = Some(newConfig)
      
    }.recover {
      case e: Exception =>
        System.err.println(s"[WARN] Cannot save config: ${e.getMessage}")
    }
  }
  
  /**
   * 从旧配置文件迁移数据
   */
  private def migrateFromOldConfigs(): Unit = {
    val oldFiles = List(
      "auto_start.txt",
      "window_settings.txt", 
      "last_avatar",
      "language_settings.txt"
    )
    
    val hasOldFiles = oldFiles.exists(file => new File(file).exists())
    
    if (hasOldFiles) {
      println("[INFO] 发现旧配置文件，正在迁移到统一配置文件...")
      
      val migratedConfig = Live2DConfig(
        window = loadOldWindowSettings(),
        model = loadOldModelSettings(),
        autoStart = loadOldAutoStartSettings(),
        language = loadOldLanguageSettings()
      )
      
      saveConfig(migratedConfig)
      
      // 备份旧文件
      oldFiles.foreach { fileName =>
        val oldFile = new File(fileName)
        if (oldFile.exists()) {
          val backupFile = new File(s"${fileName}.backup")
          oldFile.renameTo(backupFile)
          println(s"[INFO] 已备份旧配置文件: $fileName -> ${backupFile.getName}")
        }
      }
      
      println("[INFO] 配置文件迁移完成！")
    }
  }
  
  /**
   * 加载旧窗口设置
   */
  private def loadOldWindowSettings(): WindowSettings = {
    val file = new File("window_settings.txt")
    if (!file.exists()) return WindowSettings()
    
    Try {
      val source = Source.fromFile(file, "UTF-8")
      try {
        val settings = source.getLines().toList.map(_.trim).filter(_.nonEmpty)
          .flatMap { line =>
            line.split("=", 2) match {
              case Array(k, v) => Some(k -> v)
              case _ => None
            }
          }.toMap
        
        WindowSettings(
          x = settings.get("x").flatMap(_.toIntOption).getOrElse(100),
          y = settings.get("y").flatMap(_.toIntOption).getOrElse(100),
          width = settings.get("width").flatMap(_.toIntOption).getOrElse(1080),
          height = settings.get("height").flatMap(_.toIntOption).getOrElse(720),
          maximized = settings.get("maximized").flatMap(_.toBooleanOption).getOrElse(false)
        )
      } finally {
        source.close()
      }
    }.getOrElse(WindowSettings())
  }
  
  /**
   * 加载旧模型设置
   */
  private def loadOldModelSettings(): ModelSettings = {
    val file = new File("last_avatar")
    val lastAvatarPath = if (file.exists()) {
      Try {
        val source = Source.fromFile(file, "UTF-8")
        try Some(source.mkString.trim) finally source.close()
      }.getOrElse(None)
    } else None
    
    ModelSettings(lastAvatarPath = lastAvatarPath)
  }
  
  /**
   * 加载旧自动启动设置
   */
  private def loadOldAutoStartSettings(): AutoStartSettings = {
    val file = new File("auto_start.txt")
    if (!file.exists()) return AutoStartSettings()
    
    Try {
      val source = Source.fromFile(file, "UTF-8")
      try {
        val settings = source.getLines().toList.map(_.trim).filter(_.nonEmpty)
          .flatMap { line =>
            if (line.contains("=")) {
              line.split("=", 2) match {
                case Array(k, v) => Some(k -> v)
                case _ => None
              }
            } else {
              // 兼容旧格式（只有一行值的情况）
              Some("autoStart" -> line)
            }
          }.toMap
        
        AutoStartSettings(
          enabled = settings.get("autoStart").flatMap(_.toBooleanOption).getOrElse(false),
          eyeGaze = settings.get("eyeGaze").flatMap(_.toBooleanOption).getOrElse(false),
          pupilGaze = settings.get("pupilGaze").flatMap(_.toBooleanOption).getOrElse(true),
          disableEyeBlink = settings.get("disableEyeBlink").flatMap(_.toBooleanOption).getOrElse(false),
          transparentBackground = settings.get("transparentBackground").flatMap(_.toBooleanOption).getOrElse(false)
        )
      } finally {
        source.close()
      }
    }.getOrElse(AutoStartSettings())
  }
  
  /**
   * 加载旧语言设置
   */
  private def loadOldLanguageSettings(): LanguageSettings = {
    val file = new File("language_settings.txt")
    if (!file.exists()) return LanguageSettings()
    
    Try {
      val source = Source.fromFile(file, "UTF-8")
      try {
        val settings = source.getLines().toList.map(_.trim).filter(_.nonEmpty)
          .flatMap { line =>
            line.split("=", 2) match {
              case Array(k, v) => Some(k -> v)
              case _ => None
            }
          }.toMap
        
        LanguageSettings(
          language = settings.get("language").getOrElse("english")
        )
      } finally {
        source.close()
      }
    }.getOrElse(LanguageSettings())
  }
  
  // 便捷方法
  def updateWindowSettings(settings: WindowSettings): Unit = {
    val currentConfig = config
    saveConfig(currentConfig.copy(window = settings))
  }
  
  def updateModelSettings(settings: ModelSettings): Unit = {
    val currentConfig = config
    saveConfig(currentConfig.copy(model = settings))
  }
  
  def updateAutoStartSettings(settings: AutoStartSettings): Unit = {
    val currentConfig = config
    saveConfig(currentConfig.copy(autoStart = settings))
  }
  
  def updateLanguageSettings(settings: LanguageSettings): Unit = {
    val currentConfig = config
    saveConfig(currentConfig.copy(language = settings))
  }
  
  def saveLastAvatar(path: String): Unit = {
    val currentConfig = config
    val newModelSettings = currentConfig.model.copy(lastAvatarPath = Some(path))
    saveConfig(currentConfig.copy(model = newModelSettings))
  }
  
  def loadLastAvatarPath(): Option[String] = {
    config.model.lastAvatarPath
  }
  
  def saveModelParameter(paramName: String, value: Float): Unit = {
    val currentConfig = config
    val newModelSettings = currentConfig.model.copy(
      modelParameters = currentConfig.model.modelParameters + (paramName -> value)
    )
    saveConfig(currentConfig.copy(model = newModelSettings))
  }
  
  def loadModelParameters(): Map[String, Float] = {
    config.model.modelParameters
  }
}
