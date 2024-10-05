package com.google.mediapipe.examples.poselandmarker.exercises

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.activities.CameraActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityCameraBinding
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Exercise
import com.google.mediapipe.examples.poselandmarker.utils.AngleUtils
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class KneeExercise(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

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
    private var currentRepMinAngle = 180f
    private var repCount = 0
    private val maxReps = 3
    private var isFlexing = false // True if the elbow is flexing

    // List to store max ROM for each rep
    private var repMaxAngles = mutableListOf<Float>()
    private var repMinAngles = mutableListOf<Float>()
    private var angleCount = 0

    private var bindingCameraActivity: ActivityCameraBinding? = null
    private var activityMainBinding: ActivityMainBinding? = null

    private lateinit var elbowExercise: ElbowExercise

    init {
        initPaints()
        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference
    }

    // Set binding from CameraActivity
    fun setCameraActivityBinding(binding: ActivityCameraBinding) {
        this.bindingCameraActivity = binding
//        binding.btnRestart.setOnClickListener { restartExercise() }
    }

    // Set binding from MainActivity
    fun setMainActivityBinding(binding2: ActivityMainBinding) {
        this.activityMainBinding = binding2
    }

    // Function to reset the exercise for another round of 3 reps
    @SuppressLint("SetTextI18n")
    private fun restartExercise() {
        // Store exercise data after completing reps
        storeExerciseData()
        repCount = 0
        repMaxAngles.clear()
        bindingCameraActivity?.btnRestart?.visibility = View.GONE
        isFlexing = false
        currentRepMaxAngle = 0f
        bindingCameraActivity?.tvRepCount?.text = "Reps: 0"
    }

    fun clear() {
        results = null
        pointPaint.reset()
        pointPaint2.reset()
        pointPaint3.reset()
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

        // If there are no pose landmarks, return
        results?.let { poseLandmarkerResult ->
            if (poseLandmarkerResult.landmarks().isEmpty()) return

            // Iterate through all landmarks
            for (landmark in poseLandmarkerResult.landmarks()) {

                // Iterate through each normalized landmark (x, y coordinates)
                for (normalizedLandmark in landmark.withIndex()) {
                    val index = normalizedLandmark.index
                    val landmarkPosition = normalizedLandmark.value

                    // Calculate the x and y positions on the canvas
                    val xPos = landmarkPosition.x() * imageWidth * scaleFactor
                    val yPos = landmarkPosition.y() * imageHeight * scaleFactor

                    // Draw different colors based on whether the landmark is on the left, right, or center
                    when (index) {
                        // Center points
                        0, 2, 5, 7, 8, 9, 10 -> {
                            canvas.drawPoint(
                                xPos,
                                yPos,
                                pointPaint3
                            ) // Default white color for center points
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

                // Draw the connections between the pose landmarks (e.g., lines between joints)
                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start())
                            .x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start())
                            .y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end())
                            .x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end())
                            .y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
                calculateLeftKneeAngleAndReps(canvas)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun calculateLeftKneeAngleAndReps(canvas: Canvas) {
        // Make sure results are available
        results?.let { poseLandmarkerResult ->
            val landmarks = poseLandmarkerResult.landmarks().getOrNull(0) ?: return

            val hip = landmarks.get(24) ?: return // Left hip
            val knee = landmarks.get(26) ?: return // Left knee
            val ankle = landmarks.get(28) ?: return // Left ankle

            val hipPoint = PointF(
                hip.x() * imageWidth * scaleFactor,
                hip.y() * imageHeight * scaleFactor
            )
            val kneePoint = PointF(
                knee.x() * imageWidth * scaleFactor,
                knee.y() * imageHeight * scaleFactor
            )
            val anklePoint = PointF(
                ankle.x() * imageWidth * scaleFactor,
                ankle.y() * imageHeight * scaleFactor
            )

            // Draw lines for left knee with color changes based on the rep status
            if (isFlexing) {
                canvas.drawLine(
                    hipPoint.x,
                    hipPoint.y,
                    kneePoint.x,
                    kneePoint.y,
                    linePaint2
                )
                canvas.drawLine(
                    kneePoint.x,
                    kneePoint.y,
                    anklePoint.x,
                    anklePoint.y,
                    linePaint2
                )
            } else {
                canvas.drawLine(
                    hipPoint.x,
                    hipPoint.y,
                    kneePoint.x,
                    kneePoint.y,
                    linePaint
                )
                canvas.drawLine(
                    kneePoint.x,
                    kneePoint.y,
                    anklePoint.x,
                    anklePoint.y,
                    linePaint
                )
            }

            // Calculate the knee angle
            val angle = AngleUtils.calculateAngle(hipPoint, kneePoint, anklePoint)
            val kneeAngleCount = angle.toInt()

            // Update UI with the angle
            post {
                bindingCameraActivity?.tvAngle?.text = "Left Knee Angle: $kneeAngleCount"
            }

            // Call the rep tracking function for the knee
            trackRepLeftKnee(angle)
            bindingCameraActivity?.btnRestart?.setOnClickListener { restartExercise() }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun trackRepLeftKnee(angle: Double) {
        if (repCount >= maxReps) {
            bindingCameraActivity?.btnRestart?.visibility = View.VISIBLE // Show restart button after completing 3 reps
            return // Stop after 3 reps
        }

        // Update the max angle in the current rep
        if (angle > currentRepMaxAngle) {
            currentRepMaxAngle = angle.toFloat()
        }

        // Detect flexing and extension
        if (angle < 90 && !isFlexing) {
            isFlexing = true // Knee flexing phase started
            repMinAngles.add(currentRepMinAngle)
        } else if (angle > 160 && isFlexing) {
            isFlexing = false // Knee flexing phase ended, rep is complete

            // Store max ROM for this rep
            repMaxAngles.add(currentRepMaxAngle)

            // Increment rep count
            repCount++

            post {
                bindingCameraActivity?.tvRepCount?.text = "Reps: $repCount"
            }

            // Reset the max angle for the next rep
            currentRepMaxAngle = 0f
            currentRepMinAngle = 0f
        }
    }

//    @SuppressLint("SetTextI18n")
//    fun calculateLeftShoulderAngleAndReps(canvas: Canvas) {
//        // Make sure results are available
//        results?.let { poseLandmarkerResult ->
//            val landmarks = poseLandmarkerResult.landmarks().getOrNull(0) ?: return
//
//            val hip = landmarks.get(24) ?: return // Left hip
//            val shoulder = landmarks.get(12) ?: return // Left shoulder
//            val elbow = landmarks.get(14) ?: return // Left elbow
//
//            val hipPoint = PointF(
//                hip.x() * imageWidth * scaleFactor,
//                hip.y() * imageHeight * scaleFactor
//            )
//            val shoulderPoint = PointF(
//                shoulder.x() * imageWidth * scaleFactor,
//                shoulder.y() * imageHeight * scaleFactor
//            )
//            val elbowPoint = PointF(
//                elbow.x() * imageWidth * scaleFactor,
//                elbow.y() * imageHeight * scaleFactor
//            )
//
//            // Draw lines for left shoulder with color changes based on the rep status
//            if (isFlexing) {
//                canvas.drawLine(
//                    hipPoint.x,
//                    hipPoint.y,
//                    shoulderPoint.x,
//                    shoulderPoint.y,
//                    linePaint2
//                )
//                canvas.drawLine(
//                    shoulderPoint.x,
//                    shoulderPoint.y,
//                    elbowPoint.x,
//                    elbowPoint.y,
//                    linePaint2
//                )
//            } else {
//                canvas.drawLine(
//                    hipPoint.x,
//                    hipPoint.y,
//                    shoulderPoint.x,
//                    shoulderPoint.y,
//                    linePaint
//                )
//                canvas.drawLine(
//                    shoulderPoint.x,
//                    shoulderPoint.y,
//                    elbowPoint.x,
//                    elbowPoint.y,
//                    linePaint
//                )
//            }
//
//            // Calculate the shoulder angle
//            val angle = AngleUtils.calculateAngle(hipPoint, shoulderPoint, elbowPoint)
//            val shoulderAngleCount = angle.toInt()
//
//            // Update UI with the angle
//            post {
//                bindingCameraActivity?.tvAngle?.text = "Left Shoulder Angle: $shoulderAngleCount"
//            }
//
//            // Call the rep tracking function for the shoulder
//            trackRepLeftShoulder(angle)
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun trackRepLeftShoulder(angle: Double) {
//        if (repCount >= maxReps) {
//            bindingCameraActivity?.btnRestart?.visibility = View.VISIBLE // Show restart button after completing 3 reps
//            return // Stop after 3 reps
//        }
//
//        // Update the max angle in the current rep
//        if (angle > currentRepMaxAngle) {
//            currentRepMaxAngle = angle.toFloat()
//        }
//
//        // Detect flexing and extension
//        if (angle < 90 && !isFlexing) {
//            isFlexing = true // Shoulder flexing phase started
//        } else if (angle > 160 && isFlexing) {
//            isFlexing = false // Shoulder flexing phase ended, rep is complete
//
//            // Store max ROM for this rep
//            repMaxAngles.add(currentRepMaxAngle)
//
//            // Increment rep count
//            repCount++
//
//            post {
//                bindingCameraActivity?.tvRepCount?.text = "Reps: $repCount"
//            }
//
//            // Reset the max angle for the next rep
//            currentRepMaxAngle = 0f
//
//            // Store data in Firebase
//            val repData = hashMapOf(
//                "rep" to repCount,
//                "max_angle" to repMaxAngles[repCount - 1]
//            )
//            database.child("shoulder_exercise").child("rep_$repCount").setValue(repData)
//        }
//    }

    // Store exercise data function
    private fun storeExerciseData() {
        val exerciseInfo = Exercise(
            minAngle = repMinAngles.minOrNull()?.toDouble() ?: 0.0,
            maxAngle = repMaxAngles.maxOrNull()?.toDouble() ?: 0.0,
            successfulReps = repCount.toString()
        )
        // Store the exercise data using the exercise name
        FirestoreClass().storeExerciseData(context as CameraActivity, exerciseInfo, "KneeExercise")
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
