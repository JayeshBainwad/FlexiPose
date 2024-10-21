package com.google.mediapipe.examples.poselandmarker

data class ExerciseDetails(
    val exerciseName: String,   // Name of the exercise
    val minAngle: Double,       // Minimum angle of the exercise
    val maxAngle: Double,       // Maximum angle of the exercise
    val successfulReps: Int     // Number of successful repetitions
)
