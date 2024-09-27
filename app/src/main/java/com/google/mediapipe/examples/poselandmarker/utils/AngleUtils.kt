package com.google.mediapipe.examples.poselandmarker.utils

import android.graphics.PointF
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object AngleUtils {

    // Function to calculate the angle at a joint using three landmarks
    fun calculateAngle(p1: PointF, p2: PointF, p3: PointF): Float {
        val vector1 = PointF(p1.x - p2.x, p1.y - p2.y) // Vector from p1 to p2
        val vector2 = PointF(p2.x - p3.x, p2.y - p3.y) // Vector from p2 to p3

        // Dot product of the two vectors
        val dotProduct = vector1.x * vector2.x + vector1.y * vector2.y
        // Magnitudes of the vectors
        val magnitude1 = sqrt((vector1.x * vector1.x + vector1.y * vector1.y).toDouble())
        val magnitude2 = sqrt((vector2.x * vector2.x + vector2.y * vector2.y).toDouble())

        // Calculate the angle in radians between the two vectors
        val angleRadians = acos(dotProduct / (magnitude1 * magnitude2))
        // Convert radians to degrees
        var angleDegrees = Math.toDegrees(angleRadians).toFloat()

        // Constrain the angle between 0 and 180 degrees
        if (angleDegrees < 180) {
            angleDegrees = 180 - angleDegrees
        }

        return angleDegrees + 1
    }

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
