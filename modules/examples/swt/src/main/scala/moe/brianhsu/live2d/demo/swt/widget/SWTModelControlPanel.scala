package moe.brianhsu.live2d.demo.swt.widget

import moe.brianhsu.live2d.demo.app.{DemoApp, LanguageManager}
import moe.brianhsu.live2d.enitiy.model.parameter.Parameter
import moe.brianhsu.live2d.config.UnifiedConfig
import org.eclipse.swt.widgets.{Composite, TabFolder, TabItem, Button, Label, Scale, Text, Group}
import org.eclipse.swt.custom.ScrolledComposite
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.{GridLayout, GridData, FillLayout}
import org.eclipse.swt.events.{SelectionAdapter, SelectionEvent, FocusAdapter, FocusEvent, KeyAdapter, KeyEvent}
import scala.collection.mutable
import java.io.{File, PrintWriter, BufferedReader, FileReader}
import scala.util.{Try, Success, Failure}

class ModelControlPanel(parent: Composite) extends Composite(parent, SWT.NONE) {

  private var demoApp: Option[DemoApp] = None
  private var currentModelPath: Option[String] = None
  private val tabFolder = new TabFolder(this, SWT.BORDER)
  private val parameterControls = mutable.Map[String, Scale]()
  private val parameterLabels = mutable.Map[String, Label]()
  private val parameterTexts = mutable.Map[String, Text]()

  // Constructor block
  {
    this.setLayout(new FillLayout)
    tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))
    
    // Add language change listener
    LanguageManager.addLanguageChangeListener(() => updateUITexts())
    
    // Create initial empty state
    createEmptyState()
  }
  
  def setDemoApp(app: Option[DemoApp]): Unit = {
    demoApp = app
    // Update model path when demo app changes
    app.foreach { app =>
      app.avatarHolder.foreach { avatar =>
        // Extract model path from avatar settings
        val modelPath = new File(avatar.avatarSettings.mocFile).getParent
        currentModelPath = Some(modelPath)
        println(s"[ModelControl] Model path set to: $modelPath")
      }
    }
    updateParameterDisplay()
  }
  
  private def createEmptyState(): Unit = {
    // Clear existing tabs
    tabFolder.getItems.foreach(_.dispose())
    
    // Create empty state tab
    val emptyComposite = new Composite(tabFolder, SWT.NONE)
    emptyComposite.setLayout(new GridLayout(1, false))
    
    val emptyTab = new TabItem(tabFolder, SWT.NONE)
    emptyTab.setText("Model Control")
    emptyTab.setControl(emptyComposite)
    
    val emptyLabel = new Label(emptyComposite, SWT.CENTER)
    emptyLabel.setText("No model loaded")
    emptyLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true))
    
            tabFolder.layout()
  }
  
  private def updateParameterDisplay(): Unit = {
    demoApp match {
      case Some(app) =>
        app.avatarHolder.flatMap(_.model.parameters.headOption) match {
          case Some(_) =>
            createParameterTabs(app)
          case None =>
            createEmptyState()
        }
      case None =>
        createEmptyState()
    }
  }
  
  private def createParameterTabs(app: DemoApp): Unit = {
    // Clear existing tabs
    tabFolder.getItems.foreach(_.dispose())
    parameterControls.clear()
    parameterLabels.clear()
    parameterTexts.clear()
    
    val model = app.avatarHolder.get.model
    val parameters = model.parameters.values.toList
    
    // Group parameters by category
    val parameterGroups = groupParameters(parameters)
    
    // Define the desired order: Facial Expression first, Other last
    val categoryOrder = List(
      LanguageManager.getText("model_control.facial_expression"),
      LanguageManager.getText("model_control.head_pose"), 
      LanguageManager.getText("model_control.body_pose"),
      LanguageManager.getText("model_control.other")
    )
    
    // Create tabs in the specified order
    categoryOrder.foreach { category =>
      parameterGroups.get(category).foreach { params =>
        createParameterTab(category, params, model)
      }
    }
    
    // Ensure Facial Expression tab is selected first (if it exists)
    val facialExpressionCategory = LanguageManager.getText("model_control.facial_expression")
    if (parameterGroups.contains(facialExpressionCategory)) {
      val facialExpressionTab = tabFolder.getItems.find(_.getText == facialExpressionCategory)
      facialExpressionTab.foreach(tabFolder.setSelection)
    }
    
    tabFolder.layout()
    
    // Load saved parameters after creating all controls and layout
    // Use a small delay to ensure UI is fully initialized
    val display = tabFolder.getDisplay
    display.asyncExec(new Runnable {
      override def run(): Unit = {
        loadSavedParameters()
      }
    })
  }
  
  private def groupParameters(parameters: List[Parameter]): Map[String, List[Parameter]] = {
    parameters.groupBy { param =>
      val id = param.id.toLowerCase
      if (id.contains("eye")) LanguageManager.getText("model_control.facial_expression")
      else if (id.contains("mouth") || id.contains("brow") || id.contains("cheek")) LanguageManager.getText("model_control.facial_expression")
      else if (id.contains("angle") || id.contains("head")) LanguageManager.getText("model_control.head_pose")
      else if (id.contains("body") || id.contains("arm") || id.contains("hand")) LanguageManager.getText("model_control.body_pose")
      else LanguageManager.getText("model_control.other")
    }
  }
  
  private def createParameterTab(category: String, parameters: List[Parameter], model: moe.brianhsu.live2d.enitiy.model.Live2DModel): Unit = {
    // Create ScrolledComposite for scrollable content (only vertical scrollbar)
    val scrolledComposite = new ScrolledComposite(tabFolder, SWT.V_SCROLL)
    scrolledComposite.setLayout(new FillLayout)
    
    // Create content composite inside scrolled composite
    val contentComposite = new Composite(scrolledComposite, SWT.NONE)
    contentComposite.setLayout(new GridLayout(1, false)) // Single column layout for better control
    
    // Set the content of scrolled composite
    scrolledComposite.setContent(contentComposite)
    
    // Create tab item
    val tabItem = new TabItem(tabFolder, SWT.NONE)
    tabItem.setText(category)
    tabItem.setControl(scrolledComposite)
    
    // Create parameters container with two columns
    val parametersContainer = new Composite(contentComposite, SWT.NONE)
    parametersContainer.setLayout(new GridLayout(2, false))
    parametersContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true))
    
    // Create parameter controls in two columns
    parameters.zipWithIndex.foreach { case (param, index) =>
      createParameterControl(parametersContainer, param, model, index)
    }
    
    // Add Save and Reset buttons at the bottom, separated from parameters (for all tabs)
    createControlButtons(contentComposite)
    
    // Set minimum size and expand the content
    contentComposite.pack()
    scrolledComposite.setMinSize(contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT))
    scrolledComposite.setExpandHorizontal(true)
    scrolledComposite.setExpandVertical(true)
  }
  
  private def createParameterControl(parent: Composite, param: Parameter, model: moe.brianhsu.live2d.enitiy.model.Live2DModel, index: Int): Unit = {
    // Create group for parameter
    val group = new Group(parent, SWT.SHADOW_ETCHED_IN)
    group.setLayout(new GridLayout(1, false))
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    
    // Parameter name label
    val nameLabel = new Label(group, SWT.CENTER)
    nameLabel.setText(formatParameterName(param.id))
    nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    
    // Editable value text field
    val valueText = new Text(group, SWT.CENTER | SWT.BORDER)
    valueText.setText(f"${param.current}%.2f")
    valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false))
    parameterTexts(param.id) = valueText
    
    // Scale control for parameter value
    val scale = new Scale(group, SWT.HORIZONTAL)
    scale.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    scale.setMinimum((param.min * 100).toInt)
    scale.setMaximum((param.max * 100).toInt)
    scale.setSelection((param.current * 100).toInt)
    scale.setIncrement(1)
    scale.setPageIncrement(10)
    parameterControls(param.id) = scale
    
    // Helper function to update parameter value
    def updateParameterValue(newValue: Float): Unit = {
      // Clamp value to valid range
      val clampedValue = Math.max(param.min, Math.min(param.max, newValue))
      param.update(clampedValue)
      valueText.setText(f"$clampedValue%.2f")
      scale.setSelection((clampedValue * 100).toInt)
      
      // Force model update to reflect parameter changes immediately
      model.update()
      
      // Also trigger a display update to ensure visual changes are rendered
      demoApp.foreach { app =>
        app.display(true) // Force update
      }
      
      // Auto-save parameters when they change
      saveParameters()
    }
    
    // Add selection listener for scale
    scale.addSelectionListener(new SelectionAdapter {
      override def widgetSelected(e: SelectionEvent): Unit = {
        val newValue = scale.getSelection / 100.0f
        updateParameterValue(newValue)
      }
    })
    
    // Add key listener for text field (Enter key to apply)
    valueText.addKeyListener(new KeyAdapter {
      override def keyPressed(e: KeyEvent): Unit = {
        if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
          try {
            val newValue = valueText.getText.toFloat
            updateParameterValue(newValue)
          } catch {
            case _: NumberFormatException =>
              // Invalid input, revert to current value
              valueText.setText(f"${param.current}%.2f")
          }
        }
      }
    })
    
    // Add focus listener for text field (apply on focus lost)
    valueText.addFocusListener(new FocusAdapter {
      override def focusLost(e: FocusEvent): Unit = {
        try {
          val newValue = valueText.getText.toFloat
          updateParameterValue(newValue)
        } catch {
          case _: NumberFormatException =>
            // Invalid input, revert to current value
            valueText.setText(f"${param.current}%.2f")
        }
      }
    })
  }
  
  private def createControlButtons(parent: Composite): Unit = {
    // Create a group for control buttons
    val buttonGroup = new Group(parent, SWT.SHADOW_ETCHED_IN)
    buttonGroup.setLayout(new GridLayout(2, true))
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    
    // Save button
    val saveButton = new Button(buttonGroup, SWT.PUSH)
    saveButton.setText(LanguageManager.getText("model_control.save_parameters"))
    saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    saveButton.addSelectionListener(new SelectionAdapter {
      override def widgetSelected(e: SelectionEvent): Unit = {
        saveParameters()
      }
    })
    
    // Reset button
    val resetButton = new Button(buttonGroup, SWT.PUSH)
    resetButton.setText(LanguageManager.getText("model_control.reset_to_default"))
    resetButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false))
    resetButton.addSelectionListener(new SelectionAdapter {
      override def widgetSelected(e: SelectionEvent): Unit = {
        resetAllParameters()
      }
    })
  }
  
  private def formatParameterName(paramId: String): String = {
    // Get localized parameter name
    LanguageManager.getText(s"parameter.$paramId")
  }
  
  def refreshParameters(): Unit = {
    demoApp.foreach { app =>
      app.avatarHolder.foreach { avatar =>
        val model = avatar.model
        println(s"[ModelControl] Refreshing parameters, found ${parameterControls.size} controls")
        parameterControls.foreach { case (paramId, scale) =>
          model.parameters.get(paramId).foreach { param =>
            val currentValue = param.current
            scale.setSelection((currentValue * 100).toInt)
            parameterTexts.get(paramId).foreach(_.setText(f"$currentValue%.2f"))
            println(s"[ModelControl] Refreshed $paramId: $currentValue")
          }
        }
        
        // Force UI update to ensure changes are visible
        val display = tabFolder.getDisplay
        display.asyncExec(new Runnable {
          override def run(): Unit = {
            tabFolder.layout()
            tabFolder.getParent.layout()
            println(s"[ModelControl] UI layout refreshed")
          }
        })
      }
    }
  }
  
  def startRealTimeRefresh(): Unit = {
    // Real-time refresh is handled by the parameter change listeners
    // This method is kept for compatibility but doesn't need to do anything
    // since parameter updates are handled immediately when they change
  }
  
  def resetAllParameters(): Unit = {
    demoApp.foreach { app =>
      app.avatarHolder.foreach { avatar =>
        val model = avatar.model
        model.parameters.values.foreach { param =>
          param.update(param.default)
        }
        model.update()
        refreshParameters()
      }
    }
  }
  
  def saveParameters(): Unit = {
    demoApp.foreach { app =>
      app.avatarHolder.foreach { avatar =>
        val model = avatar.model
        
        // 保存所有参数到统一配置
        model.parameters.foreach { case (paramId, param) =>
          UnifiedConfig.saveModelParameter(paramId, param.current)
        }
        
        println(s"[ModelControl] Parameters saved to unified config")
      }
    }
  }
  
  def loadParameters(): Unit = {
    loadSavedParameters()
  }
  
  def updateUITexts(): Unit = {
    // Recreate parameter tabs with new language
    demoApp.foreach { app =>
      if (app.avatarHolder.isDefined) {
        createParameterTabs(app)
      }
    }
  }
  
  private def loadSavedParameters(): Unit = {
    demoApp.foreach { app =>
      app.avatarHolder.foreach { avatar =>
        val model = avatar.model
        val savedParameters = UnifiedConfig.loadModelParameters()
        var loadedCount = 0
        
        // 从统一配置加载参数
        savedParameters.foreach { case (paramId, value) =>
          model.parameters.get(paramId).foreach { param =>
            // Clamp value to valid range
            val clampedValue = Math.max(param.min, Math.min(param.max, value))
            param.update(clampedValue)
            loadedCount += 1
          }
        }
        
        // Update UI controls to reflect loaded values
        refreshParameters()
        
        // Force UI update after loading parameters
        val display = tabFolder.getDisplay
        display.asyncExec(new Runnable {
          override def run(): Unit = {
            // Force refresh of all parameter controls
            parameterControls.foreach { case (paramId, scale) =>
              model.parameters.get(paramId).foreach { param =>
                val currentValue = param.current
                scale.setSelection((currentValue * 100).toInt)
                parameterTexts.get(paramId).foreach(_.setText(f"$currentValue%.2f"))
                println(s"[ModelControl] Updated UI for $paramId: $currentValue")
              }
            }
            
            // Force layout update
            tabFolder.layout()
            tabFolder.getParent.layout()
            println(s"[ModelControl] UI layout updated after loading parameters")
          }
        })
        
        println(s"[ModelControl] Loaded $loadedCount parameters from unified config")
      }
    }
  }
}