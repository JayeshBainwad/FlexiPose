package com.google.mediapipe.examples.poselandmarker.exercises

import android.graphics.Canvas
import android.graphics.PointF
import com.google.mediapipe.examples.poselandmarker.utils.AngleUtils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ShoulderExercise {

    private var repCount = 0
    private val maxReps = 3
    private var isFlexing = false
    private var currentRepMaxAngle = 0f
    private var repMaxAngles = mutableListOf<Float>()
    private lateinit var database: DatabaseReference

    init {
        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference
    }

    fun calculateLeftShoulderAngleAndReps(
        landmarks: List<PointF>,
        canvas: Canvas,
        scaleFactor: Float,
        imageWidth: Int,
        imageHeight: Int
    ): Int {
        val hip = landmarks[24]
        val shoulder = landmarks[12]
        val elbow = landmarks[14]

        val hipPoint = PointF(
            hip.x * imageWidth * scaleFactor,
            hip.y * imageHeight * scaleFactor
        )
        val shoulderPoint = PointF(
            shoulder.x * imageWidth * scaleFactor,
            shoulder.y * imageHeight * scaleFactor
        )
        val elbowPoint = PointF(
            elbow.x * imageWidth * scaleFactor,
            elbow.y * imageHeight * scaleFactor
        )

        // Calculate the shoulder angle
        val angle = AngleUtils.calculateAngle(hipPoint, shoulderPoint, elbowPoint)
        trackRepLeftShoulder(angle)
        return angle.toInt()
    }

    private fun trackRepLeftShoulder(angle: Double) {
        if (repCount >= maxReps) {
            return
        }

        if (angle > currentRepMaxAngle) {
            currentRepMaxAngle = angle.toFloat()
        }

        if (angle < 90 && !isFlexing) {
            isFlexing = true
        } else if (angle > 160 && isFlexing) {
            isFlexing = false

            repMaxAngles.add(currentRepMaxAngle)
            repCount++
            currentRepMaxAngle = 0f

            val repData = hashMapOf("rep" to repCount, "max_angle" to repMaxAngles[repCount - 1])
            database.child("shoulder_exercise").child("rep_$repCount").setValue(repData)
        }
    }
}
