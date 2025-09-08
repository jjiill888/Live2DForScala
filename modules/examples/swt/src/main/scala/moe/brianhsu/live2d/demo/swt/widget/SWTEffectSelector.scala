package moe.brianhsu.live2d.demo.swt.widget

import moe.brianhsu.live2d.demo.app.{DemoApp, LanguageManager}
import moe.brianhsu.live2d.demo.app.DemoApp.{ClickAndDrag, FollowMouse}
import moe.brianhsu.live2d.usecase.updater.impl.EasyUpdateStrategy
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.{FillLayout, GridData, GridLayout}
import org.eclipse.swt.widgets.{Button, Combo, Composite, Event, Group}

import javax.sound.sampled.Mixer
import scala.annotation.unused

class SWTEffectSelector(parent: Composite) extends Composite(parent, SWT.NONE) {
  private var demoAppHolder: Option[DemoApp] = None

  private val effectGroup = new Group(this, SWT.SHADOW_ETCHED_IN)
  private val blink = createCheckbox(LanguageManager.getText("effects.blink"))
  private val breath = createCheckbox(LanguageManager.getText("effects.breath"))
  private val faceDirection = createCheckbox(LanguageManager.getText("effects.face_direction"), 2)
  private val faceDirectionMode = createDropdown(LanguageManager.getText("effects.click_and_drag") :: LanguageManager.getText("effects.follow_by_mouse") :: Nil)
  private val lipSyncFromMic = createCheckbox(LanguageManager.getText("effects.lip_sync"), 2)
  private val lipSyncDevice = new SWTMixerSelector(effectGroup, onMixerChanged)

  {
    this.setLayout(new FillLayout)

    effectGroup.setText(LanguageManager.getText("effects.title"))
    effectGroup.setLayout(new GridLayout(2, false))

    val deviceSelectorLayoutData = new GridData
    deviceSelectorLayoutData.horizontalAlignment = GridData.FILL
    deviceSelectorLayoutData.grabExcessHorizontalSpace = true
    deviceSelectorLayoutData.horizontalSpan = 2
    lipSyncDevice.setLayoutData(deviceSelectorLayoutData)
    lipSyncDevice.setEnabled(false)
    faceDirectionMode.setEnabled(false)

    blink.addListener(SWT.Selection, { (event: Event) => onBlinkChanged(event) })
    breath.addListener(SWT.Selection, { (event: Event) => onBreathChanged(event) })
    faceDirection.addListener(SWT.Selection, { (event: Event) => updateFaceDirectionMode(event) })
    faceDirectionMode.addListener(SWT.Selection, { (event: Event) => updateFaceDirectionMode(event) })
    lipSyncFromMic.addListener(SWT.Selection, { (event: Event) => onLipSycFromMicChanged(event) })
    lipSyncDevice.sliderControl.addChangeListener(onMicLipSyncWeightChanged)

  }

  def setDemoApp(demoApp: DemoApp): Unit = {
    this.demoAppHolder = Some(demoApp)
  }
  
  def updateUITexts(): Unit = {
    effectGroup.setText(LanguageManager.getText("effects.title"))
    blink.setText(LanguageManager.getText("effects.blink"))
    breath.setText(LanguageManager.getText("effects.breath"))
    faceDirection.setText(LanguageManager.getText("effects.face_direction"))
    lipSyncFromMic.setText(LanguageManager.getText("effects.lip_sync"))
    
    // Update dropdown items
    faceDirectionMode.removeAll()
    faceDirectionMode.add(LanguageManager.getText("effects.click_and_drag"))
    faceDirectionMode.add(LanguageManager.getText("effects.follow_by_mouse"))
  }


  def syncWithStrategy(strategy: EasyUpdateStrategy): Unit = {
    this.blink.setSelection(strategy.isEyeBlinkEnabled)
    this.breath.setSelection(strategy.isBreathEnabled)
    this.faceDirection.setSelection(strategy.isFaceDirectionEnabled)
    this.faceDirectionMode.setEnabled(strategy.isFaceDirectionEnabled)
    this.lipSyncFromMic.setSelection(false)
    this.lipSyncDevice.setEnabled(false)

    demoAppHolder.foreach { live2D =>
      live2D.faceDirectionMode match {
        case ClickAndDrag => this.faceDirectionMode.select(0)
        case FollowMouse => this.faceDirectionMode.select(1)
      }
    }
  }



  private def createCheckbox(title: String, rowSpan: Int = 1): Button = {
    val button = new Button(effectGroup, SWT.CHECK)
    val layoutData = new GridData

    layoutData.horizontalSpan = rowSpan

    button.setText(title)
    button.setLayoutData(layoutData)
    button
  }

  private def createDropdown(values: List[String]) = {
    val dropdown = new Combo(effectGroup, SWT.READ_ONLY|SWT.DROP_DOWN)
    values.foreach(dropdown.add)
    dropdown.select(0)
    dropdown
  }

  private def updateFaceDirectionMode(event: Event): Unit = {
    this.faceDirectionMode.setEnabled(this.faceDirection.getSelection)
    demoAppHolder.foreach { live2D =>
      live2D.enableFaceDirection(false)
      live2D.resetFaceDirection()
      live2D.faceDirectionMode = this.faceDirectionMode.getSelectionIndex match {
        case 0 => ClickAndDrag
        case 1 => FollowMouse
        case _ => ClickAndDrag
      }

      live2D.enableFaceDirection(this.faceDirection.getSelection)
    }
  }

  private def onLipSycFromMicChanged(event: Event): Unit = {
    lipSyncDevice.setEnabled(lipSyncFromMic.getSelection)
    demoAppHolder.foreach { demoApp =>
      if (lipSyncFromMic.getSelection) {
        lipSyncDevice.currentMixer.foreach { mixer =>
          demoApp.enableMicLipSync(
            mixer, lipSyncDevice.currentWeightPercentage,
            lipSyncDevice.isForceLipSync
          )
        }
      } else {
        demoApp.disableMicLipSync()
      }
    }

  }

  private def onMicLipSyncWeightChanged(weight: Int): Unit = {
    demoAppHolder.foreach(_.updateMicLipSyncWeight(weight))
  }

  private def onMixerChanged(mixerHolder: Option[Mixer]): Unit = {
    demoAppHolder.foreach { demoApp =>
      demoApp.disableMicLipSync()
      mixerHolder
        .filter(_ => lipSyncFromMic.getSelection)
        .foreach { mixer =>
          demoApp.enableMicLipSync(
            mixer,
            lipSyncDevice.currentWeightPercentage,
            lipSyncDevice.isForceLipSync
          )
        }
    }
  }

  private def onBreathChanged(event: Event): Unit = {
    demoAppHolder.foreach(_.enableBreath(this.breath.getSelection))
  }

  private def onBlinkChanged(event: Event): Unit = {
    demoAppHolder.foreach(_.enableEyeBlink(this.blink.getSelection))
  }
}
