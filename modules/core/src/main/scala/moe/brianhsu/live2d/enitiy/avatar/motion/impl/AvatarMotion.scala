package moe.brianhsu.live2d.enitiy.avatar.motion.impl

import moe.brianhsu.live2d.boundary.gateway.avatar.motion.MotionDataReader
import moe.brianhsu.live2d.enitiy.avatar.motion.data.{MotionCurve, MotionData}
import moe.brianhsu.live2d.enitiy.avatar.motion.impl.AvatarMotion.{EffectNameEyeBlink, EffectNameLipSync}
import moe.brianhsu.live2d.enitiy.avatar.motion.{Motion, MotionEvent}
import moe.brianhsu.live2d.enitiy.avatar.settings.detail.MotionSetting
import moe.brianhsu.live2d.enitiy.math.Easing
import moe.brianhsu.live2d.enitiy.model.Live2DModel
import moe.brianhsu.live2d.enitiy.updater.UpdateOperation
import moe.brianhsu.live2d.enitiy.updater.UpdateOperation.{FallbackParameterValueUpdate, ParameterValueUpdate}

object AvatarMotion {
  private val EffectNameEyeBlink = "EyeBlink"
  private val EffectNameLipSync  = "LipSync"

  def apply(reader: MotionDataReader, motionInfo: MotionSetting, eyeBlinkParameterIds: List[String], lipSyncParameterIds: List[String], isLoop: Boolean = false): AvatarMotion = {
    new AvatarMotion(
      reader.loadMotionData(),
      eyeBlinkParameterIds, lipSyncParameterIds,
      isLoop, isLoopFadeIn = false,
      Option(motionInfo.meta.duration).filter(_ > 0.0f),
      motionInfo.fadeInTime.filter(_ >= 0),
      motionInfo.fadeOutTime.filter(_ >= 0).orElse(Some(1.0f))
    )
  }

}

class AvatarMotion(motionData: MotionData,
                   val eyeBlinkParameterIds: List[String] = Nil,
                   val lipSyncParameterIds: List[String] = Nil,
                   override val isLoop: Boolean = false,
                   override val isLoopFadeIn: Boolean = false,
                   override val durationInSeconds: Option[Float],
                   override val fadeInTimeInSeconds: Option[Float],
                   override val fadeOutTimeInSeconds: Option[Float]) extends Motion {

  override val events: List[MotionEvent] = motionData.events


  private var cachedFadeInTime: Option[Float] = None
  private var cachedFadeOutTime: Option[Float] = None

  override def calculateOperations(model: Live2DModel, totalElapsedTimeInSeconds: Float, deltaTimeInSeconds: Float,
                                   weight: Float,
                                   startTimeInSeconds: Float,
                                   fadeInStartTimeInSeconds: Float,
                                   endTimeInSeconds: Option[Float]): List[UpdateOperation] = {

    var operations: List[UpdateOperation] = Nil

    val tmpFadeIn: Float = cachedFadeInTime.getOrElse {
      val fadeInTime = calculateTempFadeIn(totalElapsedTimeInSeconds, startTimeInSeconds)
      cachedFadeInTime = Some(fadeInTime)
      fadeInTime
    }

    val tmpFadeOut: Float = cachedFadeOutTime.getOrElse {
      val fadeOutTime = calculateTempFadeOut(totalElapsedTimeInSeconds, endTimeInSeconds)
      cachedFadeOutTime = Some(fadeOutTime)
      fadeOutTime
    }
    val elapsedTimeSinceLastLoop = calculateElapsedTimeSinceLastLoop(totalElapsedTimeInSeconds, startTimeInSeconds)

    val eyeBlinkValueHolder = motionData.modelCurves
      .filter(_.id == EffectNameEyeBlink)
      .map(curve => evaluateCurve(curve, elapsedTimeSinceLastLoop))
      .headOption

    val lipSyncValueHolder = motionData.modelCurves
      .filter(_.id == EffectNameLipSync)
      .map(curve => evaluateCurve(curve, elapsedTimeSinceLastLoop))
      .headOption

    val occupiedEyeBlinkParameterId = motionData.parameterCurves.map(_.id).intersect(eyeBlinkParameterIds).toSet
    val occupiedLipSyncParameterId = motionData.parameterCurves.map(_.id).intersect(lipSyncParameterIds).toSet

    for(curve <- motionData.parameterCurves) {
      val parameterOriginalValue: Float = model.parameters(curve.id).current

      val eyeBlinkMultiplier = eyeBlinkValueHolder.filter(_ => eyeBlinkParameterIds.contains(curve.id)).getOrElse(1.0f)
      val lipSyncValueAdder = lipSyncValueHolder.filter(_ => lipSyncParameterIds.contains(curve.id)).getOrElse(0.0f)
      val value = evaluateCurve(curve, elapsedTimeSinceLastLoop) * eyeBlinkMultiplier + lipSyncValueAdder

      val afterFading = if (curve.fadeInTime.isEmpty && curve.fadeOutTime.isEmpty) {
        parameterOriginalValue + (value - parameterOriginalValue) * weight
      } else {
        applyFading(
          totalElapsedTimeInSeconds, fadeInStartTimeInSeconds, endTimeInSeconds,
          tmpFadeIn, tmpFadeOut, curve, parameterOriginalValue, value
        )
      }

      operations = operations.appended(FallbackParameterValueUpdate(curve.id, afterFading))
    }

    operations = operations ++
      createEyeBlinkOperations(model, weight, eyeBlinkValueHolder, occupiedEyeBlinkParameterId) ++
      createLipSyncOperations(model, weight, lipSyncValueHolder, occupiedLipSyncParameterId) ++
      createPartOperations(elapsedTimeSinceLastLoop)

    operations
  }

  private def applyFading(totalElapsedTimeInSeconds: Float, fadeInStartTimeInSeconds: Float, endTimeInSeconds: Option[Float], tmpFadeIn: Float, tmpFadeOut: Float, curve: MotionCurve, parameterOriginalValue: Float, value: Float) = {
    val fadeInWeight: Float = curve.fadeInTime
      .map(fadeInTime => calculateFadeInTime(totalElapsedTimeInSeconds, fadeInStartTimeInSeconds, fadeInTime))
      .getOrElse(tmpFadeIn)

    val fadeOutWeight = curve.fadeOutTime
      .map(fadeOutTime => calculateFadeOutTime(totalElapsedTimeInSeconds, endTimeInSeconds, fadeOutTime))
      .getOrElse(tmpFadeOut)

    val paramWeight: Float = fadeInWeight * fadeOutWeight

    parameterOriginalValue + (value - parameterOriginalValue) * paramWeight
  }

  private def calculateFadeInTime(totalElapsedTimeInSeconds: Float, fadeInStartTimeInSeconds: Float, fadeInTime: Float): Float = {
    if (fadeInTime == 0.0f) {
      1.0f
    } else {
      Easing.sine((totalElapsedTimeInSeconds - fadeInStartTimeInSeconds) / fadeInTime)
    }
  }

  private def calculateFadeOutTime(totalElapsedTimeInSeconds: Float, endTimeInSeconds: Option[Float], fadeOutTime: Float): Float = {
    if (fadeOutTime == 0.0f || endTimeInSeconds.isEmpty) {
      1.0f
    } else {
      Easing.sine((endTimeInSeconds.get - totalElapsedTimeInSeconds) / fadeOutTime)
    }
  }

  private def createPartOperations(elapsedTimeSinceLastLoop: Float) = {
    motionData.partOpacityCurves
      .map { curve =>
        val value = evaluateCurve(curve, elapsedTimeSinceLastLoop)
        FallbackParameterValueUpdate(curve.id, value)
      }
  }

  private def createEyeBlinkOperations(model: Live2DModel, weight: Float, eyeBlinkValueHolder: Option[Float],
                                       occupiedEyeBlinkParameterId: Set[String]) = {
    for {
      parameterId <- eyeBlinkParameterIds if !occupiedEyeBlinkParameterId.contains(parameterId)
      parameter <- model.parameters.get(parameterId)
      eyeBlinkValue <- eyeBlinkValueHolder
      newValue = parameter.current + (eyeBlinkValue - parameter.current) * weight
    } yield {
      ParameterValueUpdate(parameter.id, newValue)
    }
  }

  private def createLipSyncOperations(model: Live2DModel, weight: Float, lipSyncValueHolder: Option[Float],
                                      occupiedLipSyncParameterId: Set[String]) = {
    for {
      parameterId <- lipSyncParameterIds if !occupiedLipSyncParameterId.contains(parameterId)
      parameter <- model.parameters.get(parameterId)
      lipSyncValue <- lipSyncValueHolder
      newValue = parameter.current + (lipSyncValue - parameter.current) * weight
    } yield {
      ParameterValueUpdate(parameter.id, newValue)
    }
  }

  private def calculateElapsedTimeSinceLastLoop(totalElapsedTimeInSeconds: Float, startTimeInSeconds: Float) = {
    // 'Repeat' time as necessary.
    var timeSinceLastLoop: Float = totalElapsedTimeInSeconds - startTimeInSeconds
    if (this.isLoop) {
      while (timeSinceLastLoop > motionData.duration) {
        timeSinceLastLoop -= motionData.duration
      }
    }
    timeSinceLastLoop
  }

  private def calculateTempFadeOut(totalElapsedTimeInSeconds: Float, endTimeInSeconds: Option[Float]) = {
    val fadeOutHolder = for {
      _ <- endTimeInSeconds
      fadeOutTime <- fadeOutTimeInSeconds if fadeOutTime > 0.0f
    } yield {
      Easing.sine((endTimeInSeconds.get - totalElapsedTimeInSeconds) / fadeOutTime)
    }
    fadeOutHolder.getOrElse(1.0f)
  }

  private def calculateTempFadeIn(totalElapsedTimeInSeconds: Float, startTimeInSeconds: Float): Float = {
    fadeInTimeInSeconds.filter(_ > 0.0f)
      .map(fadeInTime => Easing.sine((totalElapsedTimeInSeconds - startTimeInSeconds) / fadeInTime))
      .getOrElse(1.0f)
  }

  private def evaluateCurve(curve: MotionCurve, time: Float): Float = {
    val currentSegments = curve.segments
    val targetSegmentHolder = currentSegments.find(c => c.points.last.time > time)

    targetSegmentHolder match {
      case None => curve.segments.last.points.last.value
      case Some(segment) => segment.segmentType.evaluate(segment.points.toArray, time)
    }
  }
}
