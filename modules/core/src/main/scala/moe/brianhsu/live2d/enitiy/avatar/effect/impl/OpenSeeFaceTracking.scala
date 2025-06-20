package moe.brianhsu.live2d.enitiy.avatar.effect.impl

import moe.brianhsu.live2d.boundary.gateway.openSeeFace.OpenSeeFaceDataReader
import moe.brianhsu.live2d.enitiy.avatar.effect.data.OpenSeeFaceDataConverter
import moe.brianhsu.live2d.enitiy.avatar.effect.impl.FaceTracking.{TrackingNode, TrackingTaps}
import moe.brianhsu.live2d.enitiy.avatar.effect.impl.OpenSeeFaceTracking._

import scala.util.Using

object OpenSeeFaceTracking {
  val DefaultTrackingTaps: TrackingTaps = TrackingTaps(5, 5, 5, 3, 3, 3, 3, 1, 1, 7)
}

class OpenSeeFaceTracking(dataReader: OpenSeeFaceDataReader,
                          idleTimeoutInMs: Int,
                          trackingTaps: TrackingTaps = DefaultTrackingTaps,
                          dataConverter: OpenSeeFaceDataConverter = new OpenSeeFaceDataConverter) extends FaceTracking(trackingTaps) {

  private var baseNode: Option[TrackingNode] = None

  private[impl] val readerThread: ReaderThread = new ReaderThread

  def resetCalibration(): Unit = synchronized {
    baseNode = trackingNoes.headOption
  }

  override def start(): Unit = {
    readerThread.start()
  }

  override def stop(): Unit = {
    readerThread.shouldBeStopped = true
    readerThread.join()
  }

  private[impl] class ReaderThread extends Thread {
    var shouldBeStopped: Boolean = false

    override def run(): Unit = {

      Using.resource(dataReader) { reader =>

        reader.open()

        var lastUpdateTime = System.currentTimeMillis

        while (!shouldBeStopped) {
          val trackingNodeHolder = reader
            .readData()
            .map { data =>
              val leftEyePreviousNodes = trackingNoes.take(trackingTaps.leftEyeOpenness)
              val rightEyePreviousNodes = trackingNoes.take(trackingTaps.rightEyeOpenness)

              val rawNode = dataConverter.convert(
                data,
                leftEyePreviousNodes,
                rightEyePreviousNodes,
                simulateEyeGazeEnabled,
                pupilGazeEnabled
              )
              baseNode match {
                case Some(base) =>
                  rawNode.copy(
                    faceXAngle = rawNode.faceXAngle - base.faceXAngle,
                    faceYAngle = rawNode.faceYAngle - base.faceYAngle,
                    faceZAngle = rawNode.faceZAngle - base.faceZAngle,
                    transX = rawNode.transX - base.transX,
                    transY = rawNode.transY - base.transY
                  )
                case None => rawNode
              }
            }

          trackingNodeHolder.foreach { node =>
            trackingNoes = (node :: trackingNoes).take(trackingTaps.maxTaps)
            lastUpdateTime = System.currentTimeMillis
          }

          if (System.currentTimeMillis - lastUpdateTime > idleTimeoutInMs) {
            trackingNoes = Nil
          }
        }
      }
    }
  }

}
