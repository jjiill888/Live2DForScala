package moe.brianhsu.live2d.enitiy.openSeeFace

import moe.brianhsu.live2d.enitiy.math.Radian

/**
 * Estimate eye gaze direction based on OpenSeeFace 3D landmark points.
 *
 * This utility calculates the relative pupil position inside each eye
 * using the 68 point eye contour. The resulting x/y offsets are
 * normalized to the range of -1.0 to 1.0 and scaled so the
 * effect on the model is more noticeable.
 */
object EyeGazeEstimator {

  private val GazeScale = 1.5f
  private val PupilWeight = 0.4f
  private val HeadWeight = 0.6f

  private def clamp(value: Float): Float =
    Math.max(-1.0f, Math.min(1.0f, value))

  /**
   * Calculate averaged gaze offset from both eyes.
   *
   * @param data OpenSeeFaceData that contains 3D landmarks
   * @return (x, y) offsets in range [-1, 1]
   */
  def estimate(data: OpenSeeFaceData): (Float, Float) = {
    val pupilOffsets = if (data.got3DPoints) {
      val right = estimateSingle(data.points3D.slice(36, 42), data.points3D(66))
      val left  = estimateSingle(data.points3D.slice(42, 48),  data.points3D(67))
      val avgX = (right._1 + left._1) / 2
      val avgY = (right._2 + left._2) / 2
      (avgX, avgY)
    } else {
      (0.0f, 0.0f)
    }

    val yaw = Radian.radianToDegrees(data.rawEuler.y) / 30.0f
    val pitch = Radian.radianToDegrees(data.rawEuler.x) / 30.0f
    val headX = clamp(yaw)
    val headY = clamp(pitch)

    val combinedX = pupilOffsets._1 * PupilWeight + headX * HeadWeight
    val combinedY = pupilOffsets._2 * PupilWeight + headY * HeadWeight

    (clamp(combinedX * GazeScale), clamp(combinedY * GazeScale))
  }

  private def estimateSingle(eye: List[OpenSeeFaceData.Point3D], pupil: OpenSeeFaceData.Point3D): (Float, Float) = {
    val minX = eye.map(_.x).min
    val maxX = eye.map(_.x).max
    val minY = eye.map(_.y).min
    val maxY = eye.map(_.y).max

    val width = maxX - minX
    val height = maxY - minY

    val x = ((pupil.x - minX) / width * 2 - 1).toFloat
    val y = ((pupil.y - minY) / height * 2 - 1).toFloat
    (x, y)
  }
}