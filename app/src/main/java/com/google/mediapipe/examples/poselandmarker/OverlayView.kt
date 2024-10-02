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
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityCameraBinding
import com.google.mediapipe.examples.poselandmarker.utils.AngleUtils
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var pointPaint2 = Paint()
    private var pointPaint3 = Paint()

    private var linePaint = Paint()
    private var linePaint2 = Paint()
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

    private var binding: ActivityCameraBinding? = null

    // UI elements
    private var angleTextView: TextView? = null
    private var repCountTextView: TextView? = null
    private var restartButton: Button? = null

    init {
        initPaints()



        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Get the rep count TextView and restart button from the parent activity
//        if (context is CameraActivity) {
//
//            binding = ActivityCameraBinding.inflate(lay)
//
//            angleTextView = context.findViewById(R.id.tv_angle)
//            repCountTextView = context.findViewById(R.id.tv_rep_count)
//            restartButton = context.findViewById(R.id.btn_restart)
//
//            restartButton?.setOnClickListener {
//                restartExercise() // Reset the exercise when the restart button is clicked
//            }
//        }
    }

    // Set binding from CameraActivity
    fun setBinding(binding: ActivityCameraBinding) {
        this.binding = binding
        binding.btnRestart.setOnClickListener { restartExercise() }
    }

    // Function to set UI elements from the parent Activity or Fragment
//    fun setUiElements(repCountTextView: TextView, angleTextView: TextView, restartButton: Button) {
//        this.repCountTextView = repCountTextView
//        this.restartButton = restartButton
//        this.angleTextView = angleTextView
//
//        restartButton.setOnClickListener {
//            restartExercise()
//        }
//    }

    // Function to reset the exercise for another round of 3 reps
    @SuppressLint("SetTextI18n")
    private fun restartExercise() {
        repCount = 0
        repMaxAngles.clear()
        binding?.btnRestart?.visibility = View.GONE
        isFlexing = false
        currentRepMaxAngle = 0f
        binding?.tvRepCount?.text = "Reps: 0"
    }

    fun clear() {
        results = null
        pointPaint.reset()
        pointPaint2.reset()
        linePaint.reset()
        linePaint2.reset()
        textPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        linePaint2.color = ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint2.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint2.style = Paint.Style.STROKE

        pointPaint.color = ContextCompat.getColor(context!!, R.color.yellow_pose_landmarker_points)
        pointPaint2.color = ContextCompat.getColor(context!!, R.color.red_pose_landmarker_points)
        pointPaint3.color = Color.WHITE
        pointPaint.strokeWidth = LANDMARK_POINT_WIDTH
        pointPaint2.strokeWidth = LANDMARK_POINT_WIDTH
        pointPaint3.strokeWidth = LANDMARK_POINT_WIDTH
        // Set stroke cap to ROUND for circular points
        pointPaint.strokeCap = Paint.Cap.ROUND
        pointPaint2.strokeCap = Paint.Cap.ROUND
        pointPaint3.strokeCap = Paint.Cap.ROUND

        pointPaint.style = Paint.Style.FILL
        pointPaint2.style = Paint.Style.FILL
        pointPaint3.style = Paint.Style.FILL

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

//                for (normalizedLandmark in landmark) {
//                    canvas.drawPoint(
//                        normalizedLandmark.x() * imageWidth * scaleFactor,
//                        normalizedLandmark.y() * imageHeight * scaleFactor,
//                        pointPaint
//                    )
//                }

                for (normalizedLandmark in landmark.withIndex()) {
                    val index = normalizedLandmark.index
                    val landmarkPosition = normalizedLandmark.value

                    // Calculate the x and y positions on the canvas
                    val xPos = landmarkPosition.x() * imageWidth * scaleFactor
                    val yPos = landmarkPosition.y() * imageHeight * scaleFactor

                    // Assign different colors based on whether the landmark is on the left, right, or center
                    when (index) {
                        // Center points
                        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> {
                            canvas.drawPoint(xPos, yPos, pointPaint3) // Default white color for center points
                        }

                        // Left side landmarks
                        11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31 -> {
                            canvas.drawPoint(xPos, yPos, pointPaint) // Yellow for left side
                        }

                        // Right side landmarks
                        12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32 -> {
                            canvas.drawPoint(xPos, yPos, pointPaint2) // Red for right side
                        }
                    }
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

            calculateLeftElbowAngleAndReps(canvas)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateLeftElbowAngleAndReps(canvas: Canvas) {
        // Make sure results are available
        results?.let { poseLandmarkerResult ->
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

            // Draw lines for left elbow with color changes based on the rep status
            if (isFlexing) {
                canvas.drawLine(
                    shoulderPoint.x,
                    shoulderPoint.y,
                    elbowPoint.x,
                    elbowPoint.y,
                    linePaint2
                )
                canvas.drawLine(
                    elbowPoint.x,
                    elbowPoint.y,
                    wristPoint.x,
                    wristPoint.y,
                    linePaint2
                )
//                linePaint
            }

            // Calculate the elbow angle
            val angle = AngleUtils.calculateAngle(shoulderPoint, elbowPoint, wristPoint)
            angleCount = angle.toInt()

            // Update UI with the angle
            post {
                binding?.tvAngle?.text = "Left Elbow Angle: $angleCount"
            }

            // Call the rep tracking function
            trackRepLeftElbow(angle)
            binding?.btnRestart?.setOnClickListener { restartExercise() }
        }
    }


    // Function to track reps based on the elbow angle
    @SuppressLint("SetTextI18n")
    private fun trackRepLeftElbow(angle: Double) {
        if (repCount >= maxReps) {
            binding?.btnRestart?.visibility = View.VISIBLE // Show restart button after completing 3 reps
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

            post {
                binding?.tvRepCount?.text = "Reps: $repCount"
            }

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
        private const val LANDMARK_POINT_WIDTH = 32f
    }
}
