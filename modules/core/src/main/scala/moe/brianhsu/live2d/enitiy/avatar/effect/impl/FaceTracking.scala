package moe.brianhsu.live2d.enitiy.avatar.effect.impl

import moe.brianhsu.live2d.enitiy.avatar.effect.Effect
import moe.brianhsu.live2d.enitiy.avatar.effect.impl.FaceTracking.{TrackingNode, TrackingTaps}
import moe.brianhsu.live2d.enitiy.model.Live2DModel
import moe.brianhsu.live2d.enitiy.updater.UpdateOperation
import moe.brianhsu.live2d.enitiy.updater.UpdateOperation.ParameterValueUpdate

object FaceTracking {

  case class TrackingTaps(
    faceXAngle: Int, faceYAngle: Int, faceZAngle: Int,
    leftEyeOpenness: Int, rightEyeOpenness: Int,
    mouthOpenness: Int, mouthForm: Int,
    leftEyeSmile: Int, rightEyeSmile: Int,
    maxTaps: Int
  )

  case class TrackingNode(
    faceXAngle: Float, faceYAngle: Float, faceZAngle: Float,
    leftEyeOpenness: Float, rightEyeOpenness: Float,
    mouthOpenness: Float, mouthForm: Float,
    leftEyeSmile: Float, rightEyeSmile: Float,
    transX: Float = 0f, transY: Float = 0f,       //  Head translation
    eyeBallX: Float = 0f, eyeBallY: Float = 0f    //  Eye gaze offsets
  )
}

abstract class FaceTracking(protected val trackingTaps: TrackingTaps) extends Effect {

  protected[impl] var trackingNoes: List[TrackingNode] = Nil

  var eyeGazeEnabled: Boolean = true
  var eyeBlinkEnabled: Boolean = true

  override def calculateOperations(model: Live2DModel, totalElapsedTimeInSeconds: Float, deltaTimeInSeconds: Float): List[UpdateOperation] = {
    trackingNoes match {
      case Nil => Nil
      case _ => calculateOperations()
    }
  }

  private var lastFaceXAngle = 0.0f
  private var lastFaceYAngle = 0.0f
  private var lastFaceZAngle = 0.0f
  private var lastBodyXAngle = 0.0f
  private var lastBodyZAngle = 0.0f //  New: body rotation Z

  private var isFirst = true

  private def calculateOperations(): List[UpdateOperation] = {
    // Average angles and facial features
    val faceXAngle = average(trackingNoes.take(trackingTaps.faceXAngle).map(_.faceXAngle))
    val faceYAngle = average(trackingNoes.take(trackingTaps.faceYAngle).map(_.faceYAngle))
    val faceZAngle = average(trackingNoes.take(trackingTaps.faceZAngle).map(_.faceZAngle))
    val leftEyeOpenness = average(trackingNoes.take(trackingTaps.leftEyeOpenness).map(_.leftEyeOpenness))
    val rightEyeOpenness = average(trackingNoes.take(trackingTaps.rightEyeOpenness).map(_.rightEyeOpenness))
    val mouthOpenness = average(trackingNoes.take(trackingTaps.mouthOpenness).map(_.mouthOpenness))
    val mouthForm = average(trackingNoes.take(trackingTaps.mouthForm).map(_.mouthForm))
    val leftEyeSmile = average(trackingNoes.take(trackingTaps.leftEyeSmile).map(_.leftEyeSmile))
    val rightEyeSmile = average(trackingNoes.take(trackingTaps.rightEyeSmile).map(_.rightEyeSmile))
    val leftShoulder = if (this.lastFaceXAngle < -10) 1.0f else 0.0f
    val rightShoulder = if (this.lastFaceXAngle > 10) 1.0f else 0.0f


    //  New: Average translation
    val transX = average(trackingNoes.map(_.transX))
    val transY = average(trackingNoes.map(_.transY))
    val eyeBallX = if (eyeGazeEnabled) average(trackingNoes.map(_.eyeBallX)) else 0f
    val eyeBallY = if (eyeGazeEnabled) average(trackingNoes.map(_.eyeBallY)) else 0f

    if (isFirst) {
      this.lastFaceXAngle = faceXAngle
      this.lastFaceYAngle = faceYAngle
      this.lastFaceZAngle = faceZAngle
      this.lastBodyXAngle = faceXAngle
      this.lastBodyZAngle = faceZAngle
      this.isFirst = false
    }

    // Smooth head rotation
    this.lastFaceXAngle = lastFaceXAngle + calculateNewDiff(faceXAngle - lastFaceXAngle)
    this.lastFaceYAngle = lastFaceYAngle + calculateNewDiff(faceYAngle - lastFaceYAngle)
    this.lastFaceZAngle = lastFaceZAngle + calculateNewDiff(faceZAngle - lastFaceZAngle)

    // Smooth body movement
    this.lastBodyXAngle = lastBodyXAngle + calculateNewDiff(faceXAngle - lastBodyXAngle, 0.85f)
    this.lastBodyZAngle = lastBodyZAngle + calculateNewDiff(faceZAngle - lastBodyZAngle, 0.85f)

    val eyeOpenUpdates =
      if (eyeBlinkEnabled) List(
        ParameterValueUpdate("ParamEyeLOpen", leftEyeOpenness),
        ParameterValueUpdate("ParamEyeROpen", rightEyeOpenness)
      ) else Nil

    val result = List(
      ParameterValueUpdate("ParamAngleX", this.lastFaceXAngle),
      ParameterValueUpdate("ParamAngleY", this.lastFaceYAngle),
      ParameterValueUpdate("ParamAngleZ", this.lastFaceZAngle),
      ParameterValueUpdate("ParamBodyAngleX", this.lastBodyXAngle, 0.75f),
      ParameterValueUpdate("ParamBodyAngleZ", this.lastBodyZAngle, 0.75f), //  New
      ParameterValueUpdate("ParamBodyX", transX),                           //  New
      ParameterValueUpdate("ParamBodyY", transY),                           //  New
      ParameterValueUpdate("ParamBodyAngleY", this.lastFaceYAngle * 0.5f, 0.6f),  //  New
      ParameterValueUpdate("ParamBodyAngleZ", this.lastFaceZAngle * 0.3f, 0.6f),  //  New
      ParameterValueUpdate("ParamAllX", this.lastFaceXAngle * 0.08f, 0.4f),  //  New
      ParameterValueUpdate("ParamAllY", this.lastFaceYAngle * 0.06f, 0.4f),  //  New
      ParameterValueUpdate("ParamAllRotate", this.lastFaceZAngle * 0.25f, 0.4f),  //  New
      ParameterValueUpdate("ParamLeftShoulderUp", leftShoulder, 0.3f),  //  New
      ParameterValueUpdate("ParamRightShoulderUp", rightShoulder, 0.3f),  //  New
      ParameterValueUpdate("ParamMouthOpenY", mouthOpenness),
      ParameterValueUpdate("ParamMouthForm", mouthForm),
      ParameterValueUpdate("ParamEyeLSmile", leftEyeSmile),
      ParameterValueUpdate("ParamEyeRSmile", rightEyeSmile),
      ParameterValueUpdate("ParamEyeBallX", eyeBallX),
      ParameterValueUpdate("ParamEyeBallY", eyeBallY)
    ) ++ eyeOpenUpdates

    result
  }

  // Smooth the motion to prevent jittering
  private def calculateNewDiff(originalDiff: Float, maxDiff: Float = 2.65f): Float = {
    val afterLog = Math.log1p(originalDiff.abs).min(maxDiff).toFloat
    if (originalDiff > 0) {
      originalDiff.min(afterLog)
    } else if (originalDiff < 0){
      originalDiff.max(-afterLog)
    } else {
      originalDiff
    }
  }

  // Average helper
  private def average(values: List[Float]): Float = values.sum / values.size
}
