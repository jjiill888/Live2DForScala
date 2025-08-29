package moe.brianhsu.live2d.demo.swing.widget

import moe.brianhsu.live2d.demo.openSeeFace.{CameraListing, ExternalOpenSeeFaceDataReader, OpenSeeFaceSetting}
import moe.brianhsu.live2d.demo.app.DemoApp
import moe.brianhsu.live2d.demo.swing.Live2DUI
import moe.brianhsu.live2d.demo.swing.widget.faceTracking.{SwingOpenSeeFaceAdvance, SwingOpenSeeFaceBundle, SwingOpenSeeFacePlaceholder}
import moe.brianhsu.live2d.enitiy.openSeeFace.OpenSeeFaceData
import moe.brianhsu.live2d.enitiy.openSeeFace.OpenSeeFaceData.Point

import java.awt.event.ActionEvent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.awt.{BasicStroke, CardLayout, Color, Graphics, Graphics2D, GridBagConstraints, GridBagLayout}
import java.util.concurrent.{Callable, ScheduledThreadPoolExecutor, TimeUnit}
import javax.swing._
import scala.annotation.unused
import scala.util.Try

class SwingFaceTrackingPane(live2DWidget: Live2DUI) extends JPanel {
  private var openSeeFaceDataHolder: Option[OpenSeeFaceData] = None
  private var openSeeFaceReaderHolder: Option[ExternalOpenSeeFaceDataReader] = None

  private val openSeeFaceModeLabel = new JLabel("Mode:")
  private val openSeeFaceModeCombo = new JComboBox[String](Array("Bundle", "Advanced"))
  private val openSeeFaceAdvance = new SwingOpenSeeFaceAdvance
  private val openSeeFaceBundle = createBundleByOS()
  private val cardLayout = new CardLayout
  private val openSeeFacePanel = new JPanel(cardLayout)
  private val disableEyeBlinkCheckBox = new JCheckBox("Disable Eye Blink")
  disableEyeBlinkCheckBox.setSelected(DemoApp.loadDisableEyeBlink())
  disableEyeBlinkCheckBox.addActionListener(_ => {
    DemoApp.saveDisableEyeBlink(disableEyeBlinkCheckBox.isSelected)
    live2DWidget.demoAppHolder.foreach(_.enableTrackingEyeBlink(!disableEyeBlinkCheckBox.isSelected))
  })
  private val simulateEyeGazeCheckBox = new JCheckBox("Simulate Eye Gaze")
  simulateEyeGazeCheckBox.setSelected(DemoApp.loadEyeGaze())
  simulateEyeGazeCheckBox.addActionListener(_ => {
    DemoApp.saveEyeGaze(simulateEyeGazeCheckBox.isSelected)
    live2DWidget.demoAppHolder.foreach(_.enableSimulateEyeGaze(simulateEyeGazeCheckBox.isSelected))
  })
  private val pupilGazeCheckBox = new JCheckBox("Gaze Tracking")
  pupilGazeCheckBox.setSelected(DemoApp.loadPupilGaze())
  pupilGazeCheckBox.addActionListener(_ => {
    DemoApp.savePupilGaze(pupilGazeCheckBox.isSelected)
    live2DWidget.demoAppHolder.foreach(_.enablePupilGaze(pupilGazeCheckBox.isSelected))
  })
  private val autoStartCheckBox = new JCheckBox("Auto Start")
  autoStartCheckBox.setSelected(DemoApp.loadAutoStart())
  autoStartCheckBox.addActionListener(_ => DemoApp.saveAutoStart(autoStartCheckBox.isSelected))
  private val webcamResetButton = new JButton("Webcam reset")
  webcamResetButton.addActionListener(_ =>
    live2DWidget.demoAppHolder.foreach(_.resetWebcamCalibration())
  )
  private val (startButton, stopButton, buttonPanel, buttonCardLayout) = createStartStopButton()
  private val outlinePanel = new OutlinePanel
  private val executor = new ScheduledThreadPoolExecutor(1)

  {
    this.setLayout(new GridBagLayout)
    val gc1 = new GridBagConstraints()
    gc1.gridx = 0
    gc1.gridy = 0
    gc1.anchor = GridBagConstraints.NORTHWEST
    this.add(openSeeFaceModeLabel, gc1)

    val gc2 = new GridBagConstraints()
    gc2.gridx = 1
    gc2.gridy = 0
    gc2.anchor = GridBagConstraints.NORTHWEST
    gc2.fill = GridBagConstraints.HORIZONTAL
    gc2.weightx = 1
    this.add(openSeeFaceModeCombo)

    val gc3 = new GridBagConstraints()
    gc3.gridx = 0
    gc3.gridy = 1
    gc3.gridwidth = 2
    gc3.fill = GridBagConstraints.HORIZONTAL
    gc3.weightx = 1
    gc2.anchor = GridBagConstraints.NORTHWEST
    this.openSeeFacePanel.add(openSeeFaceBundle, "Bundle")
    this.openSeeFacePanel.add(openSeeFaceAdvance, "Advanced")
    this.add(openSeeFacePanel, gc3)

    val gcBlink = new GridBagConstraints()
    gcBlink.gridx = 0
    gcBlink.gridy = 3
    gcBlink.gridwidth = 2
    gcBlink.anchor = GridBagConstraints.NORTHWEST
    this.add(disableEyeBlinkCheckBox, gcBlink)

    val gcGaze = new GridBagConstraints()
    gcGaze.gridx = 0
    gcGaze.gridy = 4
    gcGaze.gridwidth = 2
    gcGaze.anchor = GridBagConstraints.NORTHWEST
    this.add(simulateEyeGazeCheckBox, gcGaze)

    val gcRealGaze = new GridBagConstraints()
    gcRealGaze.gridx = 0
    gcRealGaze.gridy = 5
    gcRealGaze.gridwidth = 2
    gcRealGaze.anchor = GridBagConstraints.NORTHWEST
    this.add(pupilGazeCheckBox, gcRealGaze)
    
    val gcAuto = new GridBagConstraints()
    gcAuto.gridx = 0
    gcAuto.gridy = 2
    gcAuto.gridwidth = 2
    gcAuto.anchor = GridBagConstraints.NORTHWEST
    this.add(autoStartCheckBox, gcAuto)

    val gc4 = new GridBagConstraints()
    gc4.gridx = 0
    gc4.gridy = 5
    gc4.gridwidth = 2
    gc4.fill = GridBagConstraints.HORIZONTAL
    gc4.weightx = 1
    gc4.anchor = GridBagConstraints.NORTHWEST
    this.add(buttonPanel, gc4)

    val gcReset = new GridBagConstraints()
    gcReset.gridx = 0
    gcReset.gridy = 6
    gcReset.gridwidth = 2
    gcReset.anchor = GridBagConstraints.NORTHWEST
    this.add(webcamResetButton, gcReset)

    val gc5 = new GridBagConstraints()
    gc5.gridx = 0
    gc5.gridy = 7
    gc5.gridwidth = 2
    gc5.fill = GridBagConstraints.BOTH
    gc5.weightx = 1
    gc5.weighty = 1
    gc5.anchor = GridBagConstraints.NORTHWEST
    this.add(outlinePanel, gc5)

    this.openSeeFaceModeCombo.addActionListener { (event: ActionEvent) => onModeSelected(event) }
    this.startButton.addActionListener { (event: ActionEvent) => onStartSelected(event) }
    this.stopButton.addActionListener { (event: ActionEvent) => onStopSelected(event) }

  }

  def enableStartButton(): Unit = {
    this.startButton.setEnabled(true)
        if (autoStartCheckBox.isSelected) {
      onStartSelected(new ActionEvent(autoStartCheckBox, ActionEvent.ACTION_PERFORMED, "AutoStart"))
    }
  }

  private def createBundleByOS() = {
    if (System.getProperty("os.name").toLowerCase.contains("mac")) {
      new SwingOpenSeeFacePlaceholder
    } else {
      new SwingOpenSeeFaceBundle(CameraListing.createByOS())
    }
  }

  private def onStartSelected(event: ActionEvent): Unit = {
    live2DWidget.demoAppHolder.foreach(_.disableFaceTracking())
    this.openSeeFaceReaderHolder.foreach(_.close())
    this.openSeeFaceReaderHolder = None

    val settings = getOpenSeeFaceSetting

    ExternalOpenSeeFaceDataReader
      .startAsync(settings.getCommand, settings.getHostname, settings.getPort, onDataRead)
      .onComplete {
        case Success(reader) =>
          this.openSeeFaceReaderHolder = Some(reader)
          for (demoApp <- live2DWidget.demoAppHolder) demoApp.enableFaceTracking(reader)
          SwingUtilities.invokeLater { () =>
            startButton.setEnabled(false)
            stopButton.setEnabled(true)
            buttonCardLayout.show(buttonPanel, "Stop")
            outlinePanel.update(outlinePanel.getGraphics)
          }
        case Failure(e) =>
          SwingUtilities.invokeLater { () =>
            JOptionPane.showMessageDialog(this, e.getMessage, "Failed to start OpenSeeFace", JOptionPane.ERROR_MESSAGE)
          }
      }

  }

  private def onStopSelected(event: ActionEvent): Unit = {
    this.live2DWidget.demoAppHolder.foreach(_.disableFaceTracking())
    this.openSeeFaceReaderHolder = None
    this.openSeeFaceReaderHolder.foreach(_.close())

    this.openSeeFaceDataHolder = None

    this.outlinePanel.update(this.outlinePanel.getGraphics)

    this.startButton.setEnabled(true)
    this.stopButton.setEnabled(false)
    this.buttonCardLayout.show(buttonPanel, "Start")
  }

  private def onDataRead(data: OpenSeeFaceData): Unit = {
    this.openSeeFaceDataHolder = Some(data)
    SwingUtilities.invokeLater { () =>
      this.outlinePanel.repaint()
    }
  }

  private def getOpenSeeFaceSetting: OpenSeeFaceSetting = {
    this.openSeeFaceModeCombo.getSelectedIndex match {
      case 0 => openSeeFaceBundle
      case 1 => openSeeFaceAdvance
    }
  }

  private class OutlinePanel extends JPanel {

    this.setBorder(BorderFactory.createTitledBorder("Outline"))

    override def paintComponent(g: Graphics): Unit = {

      super.paintComponent(g)
      if (!this.isShowing || !this.isDisplayable) {
        return
      }

      val gc = g.asInstanceOf[Graphics2D]
      val width = this.getBounds().width
      val height = this.getBounds().width


      if (openSeeFaceReaderHolder.isDefined && openSeeFaceDataHolder.isEmpty) {

        if (System.currentTimeMillis() / 500 % 2 == 0) {
          gc.setColor(new Color(255, 0, 0))
          gc.fillOval(20, 20, 20, 20)
        }


        executor.schedule(new Callable[Unit] {
          override def call(): Unit = {
            SwingUtilities.invokeLater { () =>
              if (OutlinePanel.this.isValid) {
                OutlinePanel.this.repaint()
              }
            }
          }
        }, 500, TimeUnit.MILLISECONDS)
      }

      openSeeFaceDataHolder.foreach { data =>

        if (System.currentTimeMillis() / 500 % 2 == 0) {
          gc.setColor(new Color(255, 0, 0))
          gc.fillOval(20, 20, 20, 20)
        }

        gc.setColor(new Color(0, 0, 255))

        val scaleX = width / data.resolution.width
        val scaleY = height / data.resolution.height


        def convert(p: Point): List[Int] = {
          List((p.x * scaleX).toInt , (p.y * scaleY).toInt)
        }

        val (faceOutlineX, faceOutlineY) = splitXY(data.landmarks.slice(0, 17).flatMap(convert).toArray)
        val (noseVerticalX, noseVerticalY) = splitXY(data.landmarks.slice(27, 31).flatMap(convert).toArray)
        val (noseHorizontalX, noseHorizontalY) = splitXY(data.landmarks.slice(31, 36).flatMap(convert).toArray)
        val (rightEyeBrowX, rightEyeBrowY) = splitXY(data.landmarks.slice(17, 22).flatMap(convert).toArray)
        val (leftEyeBrowX, leftEyeBrowY) = splitXY(data.landmarks.slice(22, 27).flatMap(convert).toArray)
        val (rightEyeX, rightEyeY) = splitXY(data.landmarks.slice(36, 42).appended(data.landmarks(36)).flatMap(convert).toArray)
        val (leftEyeX, leftEyeY) = splitXY(data.landmarks.slice(42, 48).appended(data.landmarks(42)).flatMap(convert).toArray)
        val (mouthX, mouthY) = splitXY(data.landmarks.slice(48, 59).appended(data.landmarks(48)).flatMap(convert).toArray)
        val (upperLipX, upperLipY) = splitXY(data.landmarks.slice(59, 62).flatMap(convert).toArray)
        val (lowerLipX, lowerLipY) = splitXY(data.landmarks.slice(63, 66).flatMap(convert).toArray)

        gc.setStroke(new BasicStroke(2))

        gc.drawPolyline(faceOutlineX, faceOutlineY, faceOutlineX.length)
        gc.drawPolyline(noseVerticalX, noseVerticalY, noseVerticalX.length)
        gc.drawPolyline(noseHorizontalX, noseHorizontalY, noseHorizontalX.length)
        gc.drawPolyline(rightEyeBrowX, rightEyeBrowY, rightEyeBrowX.length)
        gc.drawPolyline(leftEyeBrowX, leftEyeBrowY, leftEyeBrowX.length)
        gc.drawPolyline(rightEyeX, rightEyeY, rightEyeX.length)
        gc.drawPolyline(leftEyeX, leftEyeY, leftEyeX.length)
        gc.drawPolyline(mouthX, mouthY, mouthX.length)
        gc.drawPolyline(upperLipX, upperLipY, upperLipX.length)
        gc.drawPolyline(lowerLipX, lowerLipY, lowerLipX.length)
      }

    }

    private def splitXY(points: Array[Int]): (Array[Int], Array[Int]) = {
      val (xWithIndex, yWithIndex) = points.zipWithIndex.partition(_._2 % 2 == 0)
      (xWithIndex.map(_._1), yWithIndex.map(_._1))
    }

  }

  private def onModeSelected(actionEvent: ActionEvent): Unit = {
    this.cardLayout.show(openSeeFacePanel, this.openSeeFaceModeCombo.getSelectedItem.toString)
  }

  private def createStartStopButton(): (JButton, JButton, JPanel, CardLayout) = {
    val cardLayout = new CardLayout
    val panel = new JPanel(cardLayout)

    val startButton = new JButton("Start")
    val stopButton = new JButton("Stop")

    stopButton.setEnabled(false)
    startButton.setEnabled(false)
    panel.add(startButton, "Start")
    panel.add(stopButton, "Stop")

    (startButton, stopButton, panel, cardLayout)
  }

}
