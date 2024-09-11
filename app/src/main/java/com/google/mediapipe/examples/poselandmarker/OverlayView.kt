package com.google.mediapipe.examples.poselandmarker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
//import org.checkerframework.checker.units.qual.Angle
import kotlin.math.*

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // Firebase reference
    private lateinit var database: DatabaseReference

    // Variables for tracking reps and ROM
    private var currentRepMaxAngle = 0f
    private var repCount = 0
    private val maxReps = 3
    private var isFlexing = false // True if the elbow is flexing

    // List to store max ROM for each rep
    private var repMaxAngles = mutableListOf<Float>()
    private var angleCount = 0

    // UI elements
    private var angleTextView: TextView? = null
    private var repCountTextView: TextView? = null
    private var restartButton: Button? = null

    init {
        initPaints()

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Get the rep count TextView and restart button from the parent activity
        if (context is MainActivity) {
            angleTextView = context.findViewById(R.id.right_elbow_angle)
            repCountTextView = context.findViewById(R.id.rep_count)
            restartButton = context.findViewById(R.id.restart_button)

            restartButton?.setOnClickListener {
                restartExercise() // Reset the exercise when the restart button is clicked
            }
        }
    }

    // Function to set UI elements from the parent Activity or Fragment
    fun setUiElements(repCountTextView: TextView, restartButton: Button) {
        this.repCountTextView = repCountTextView
        this.restartButton = restartButton

        restartButton.setOnClickListener {
            restartExercise()
        }
    }

    // Function to reset the exercise for another round of 3 reps
    @SuppressLint("SetTextI18n")
    private fun restartExercise() {
        repCount = 0
        repMaxAngles.clear()
        restartButton?.visibility = View.GONE
        isFlexing = false
        currentRepMaxAngle = 0f
        repCountTextView?.text = "Reps: 0"
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

    @SuppressLint("SetTextI18n")
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            if (poseLandmarkerResult.landmarks().isEmpty()) return

            for (landmark in poseLandmarkerResult.landmarks()) {
                for (normalizedLandmark in landmark) {
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
                        linePaint
                    )
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
            angleCount = angle.toInt()
            angleTextView?.text = "Angle: $angleCount"

            // Check if it's a valid rep
            trackRep(angle)

            // Display angle
//            canvas.drawText(
//                "${angle.toInt()}°",
//                elbowPoint.x,
//                elbowPoint.y - 10,
//                textPaint
//            )
        }
    }

    // Function to track reps based on the elbow angle
    @SuppressLint("SetTextI18n")
    private fun trackRep(angle: Float) {
        if (repCount >= maxReps) {
            restartButton?.visibility = View.VISIBLE // Show restart button after completing 3 reps
            return // Stop after 3 reps
        }

        // Update the max angle in the current rep
        if (angle > currentRepMaxAngle) {
            currentRepMaxAngle = angle
        }

        // Detect rep completion: when the elbow is extended after flexing
        if (angle < 30 && isFlexing) {
            // Rep completed, store the max angle for the rep
            repMaxAngles.add(currentRepMaxAngle)

            // Reset for the next rep
            repCount++
            currentRepMaxAngle = 0f
            isFlexing = false

            // Update the rep count TextView
            repCountTextView?.text = "Reps: $repCount"


            // Store data in Firebase if 3 reps are done
            if (repCount == maxReps) {
                storeMaxAnglesInFirebase(repMaxAngles)
            }
        } else if (angle > 150) {
            isFlexing = true // The elbow is flexing
        }
    }

    // Function to store max angles in Firebase after 3 reps
    private fun storeMaxAnglesInFirebase(maxAngles: List<Float>) {
        val angleData: Map<String, Any> = mapOf(
            "rep1_max_angle" to (maxAngles.getOrNull(0) ?: 0f),
            "rep2_max_angle" to (maxAngles.getOrNull(1) ?: 0f),
            "rep3_max_angle" to (maxAngles.getOrNull(2) ?: 0f)
        )

        database.child("pose_data").push().setValue(angleData)
            .addOnSuccessListener {
                Log.d("FirebaseDataStore", "Data stored successfully: $angleData")
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseDataStore", "Failed to store data", exception)
            }
    }


    private fun calculateAngle(p1: PointF, p2: PointF, p3: PointF): Float {
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
        private const val LANDMARK_STROKE_WIDTH = 10f
    }
}
