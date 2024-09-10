package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.google.mediapipe.util.proto.RenderDataProto.RenderAnnotation.Text
import kotlin.math.*

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        textPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.RED
        textPaint.textSize = 60f
        textPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            if (poseLandmarkerResult.landmarks().isEmpty()) return

            for(landmark in poseLandmarkerResult.landmarks()) {
                for(normalizedLandmark in landmark) {
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint)
                    }
                }

                // Right elbow landmarks: shoulder (12), elbow (14), wrist (16)
                val landmarks = poseLandmarkerResult.landmarks().getOrNull(0) ?: return

                val shoulder = landmarks.get(12) ?: return
                val elbow = landmarks.get(14) ?: return
                val wrist = landmarks.get(16) ?: return

                val shoulderPoint = PointF(
                    shoulder.x() * imageWidth * scaleFactor,
                    shoulder.y() * imageHeight * scaleFactor
                )
                val elbowPoint = PointF(
                    elbow.x() * imageWidth * scaleFactor,
                    elbow.y() * imageHeight * scaleFactor
                )
                val wristPoint = PointF(
                    wrist.x() * imageWidth * scaleFactor,
                    wrist.y() * imageHeight * scaleFactor
                )

                // Draw lines for the right elbow
                canvas.drawLine(
                    shoulderPoint.x,
                    shoulderPoint.y,
                    elbowPoint.x,
                    elbowPoint.y,
                    pointPaint
                )
                canvas.drawLine(
                    elbowPoint.x,
                    elbowPoint.y,
                    wristPoint.x,
                    wristPoint.y,
                    pointPaint
                )

                // Calculate angle at the elbow
                val angle = calculateAngle(shoulderPoint, elbowPoint, wristPoint)

                // Display angle
                canvas.drawText(
                    "${angle.toInt()}°",
                    elbowPoint.x,
                    elbowPoint.y - 10,
                    textPaint
                )

        }
    }


    private fun calculateAngle(p1: PointF, p2: PointF, p3: PointF): Float {
        val vector1 = PointF(p1.x - p2.x, p1.y - p2.y)  // Vector from p1 to p2
        val vector2 = PointF(p2.x - p3.x, p2.y - p3.y)  // Vector from p2 to p3

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


    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}
