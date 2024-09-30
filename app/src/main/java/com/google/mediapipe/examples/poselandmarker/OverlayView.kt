package com.google.mediapipe.examples.poselandmarker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mediapipe.examples.poselandmarker.utils.AngleUtils
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

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
//        if (context is MainActivity) {
//            angleTextView = context.findViewById(R.id.tv_right_elbow_angle)
//            repCountTextView = context.findViewById(R.id.tv_rep_count)
//            restartButton = context.findViewById(R.id.restart_button)
//
//            restartButton?.setOnClickListener {
//                restartExercise() // Reset the exercise when the restart button is clicked
//            }
//        }
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

            // Calculate angle at the elbow using AngleUtils
            val angle = AngleUtils.calculateAngle(shoulderPoint, elbowPoint, wristPoint)
            angleCount = angle.toInt()
            angleTextView?.text = "Angle: $angleCount"

            // Check if it's a valid rep
            trackRep(angle)
        }
    }

    // Function to track reps based on the elbow angle
    @SuppressLint("SetTextI18n")
    private fun trackRep(angle: Double) {
        if (repCount >= maxReps) {
            restartButton?.visibility = View.VISIBLE // Show restart button after completing 3 reps
            return // Stop after 3 reps
        }

        // Update the max angle in the current rep
        if (angle > currentRepMaxAngle) {
            currentRepMaxAngle = angle.toFloat()
        }

        // Detect flexing and extension
        if (angle < 90 && !isFlexing) {
            isFlexing = true // Flexing phase started
        } else if (angle > 160 && isFlexing) {
            isFlexing = false // Flexing phase ended, rep is complete

            // Store max ROM for this rep
            repMaxAngles.add(currentRepMaxAngle)

            // Increment rep count
            repCount++
            repCountTextView?.text = "Reps: $repCount"

            // Reset the max angle for the next rep
            currentRepMaxAngle = 0f

            // Store data in Firebase
            val repData = hashMapOf(
                "rep" to repCount,
                "max_angle" to repMaxAngles[repCount - 1]
            )
            database.child("elbow_exercise").child("rep_$repCount").setValue(repData)
        }
    }

    fun setResults(
        poseLandmarkerResult: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode
    ) {
        this.results = poseLandmarkerResult
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        scaleFactor = AngleUtils.calculateScaleFactor(
            imageWidth,
            imageHeight,
            this.width,
            this.height,
            runningMode
        )
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8f
    }
}
