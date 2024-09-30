package com.google.mediapipe.examples.poselandmarker.utils

import android.graphics.PointF
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

object AngleUtils {
    // Function to calculate angle between three landmarks
    fun calculateAngle(
        firstLandmark: PointF,
        midLandmark: PointF,
        thirdLandmark: PointF
    ): Double {
        val x1 = firstLandmark.x
        val y1 = firstLandmark.y
        val x2 = midLandmark.x
        val y2 = midLandmark.y
        val x3 = thirdLandmark.x
        val y3 = thirdLandmark.y

        val angleRad = atan2(y3 - y2, x3 - x2) - atan2(y1 - y2, x1 - x2)
        val angleDeg = Math.toDegrees(angleRad.toDouble())

        // Normalize angle to range 0-360
        return if (angleDeg < 0) angleDeg + 360 else angleDeg
    }

    // Example usage
//    fun main() {
//        // Assuming you have obtained landmarks using MediaPipe Pose Landmarker
//        val shoulderLandmark = LandmarkProto.Landmark.newBuilder().setX(0.5f).setY(0.5f).build()
//        val elbowLandmark = LandmarkProto.Landmark.newBuilder().setX(0.6f).setY(0.7f).build()
//        val wristLandmark = LandmarkProto.Landmark.newBuilder().setX(0.7f).setY(0.8f).build()
//
//        val angle = calculateAngle(shoulderLandmark, elbowLandmark, wristLandmark)
//        println("Angle: $angle degrees")
//    }

    // Function to calculate the scale factor
    fun calculateScaleFactor(imageWidth: Int, imageHeight: Int, viewWidth: Int, viewHeight: Int, runningMode: RunningMode): Float {
        return when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(viewWidth * 1f / imageWidth, viewHeight * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(viewWidth * 1f / imageWidth, viewHeight * 1f / imageHeight)
            }
        }
    }
}
