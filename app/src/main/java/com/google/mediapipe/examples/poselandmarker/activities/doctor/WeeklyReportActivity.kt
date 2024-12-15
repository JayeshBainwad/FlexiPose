package com.google.mediapipe.examples.poselandmarker.activities.doctor

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.examples.poselandmarker.ExerciseData
import com.google.mediapipe.examples.poselandmarker.WeeklyReportAdapter
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityWeeklyReportBinding
import com.google.mediapipe.examples.poselandmarker.model.Exercise
import com.google.mediapipe.examples.poselandmarker.utils.Constants
import java.time.LocalDate

class WeeklyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyReportBinding
    private lateinit var weeklyReportAdapter: WeeklyReportAdapter
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        binding.recyclerViewWeeklyReport.layoutManager = LinearLayoutManager(this)

        // Load weekly data
        loadWeeklyData()
    }

    private fun loadWeeklyData() {
        val patientId = intent.getStringExtra("PATIENT_ID")
        if (patientId == null) {
            Log.e("loadWeeklyData", "Patient ID is null.")
            return
        }

        val exerciseDataList = mutableListOf<Triple<String, String, Exercise>>() // Triple<Exercise Name, Date, Exercise Details>
        val exerciseNames = listOf("ElbowExercise", "KneeExercise", "ShoulderExercise")

        // Calculate the date 7 days ago
        val sevenDaysAgo = LocalDate.now().minusDays(7).toString()

        for (exerciseName in exerciseNames) {
            firestore.collection(Constants.PATIENTUSERS)
                .document(patientId)
                .collection(exerciseName)
                .whereGreaterThanOrEqualTo("date", sevenDaysAgo) // Only fetch past 7 days' data
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val exercise = document.toObject(Exercise::class.java)
                        if (exercise != null) {
                            exerciseDataList.add(Triple(exerciseName, exercise.date, exercise))
                        }
                    }

                    // If all exercises are fetched, populate the RecyclerView
                    if (exerciseDataList.isNotEmpty()) {
                        processAndSetData(exerciseDataList)
                    } else {
                        Log.d("loadWeeklyData", "No exercises found in the past 7 days.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("loadWeeklyData", "Error fetching exercises from $exerciseName", e)
                }
        }
    }

    private fun processAndSetData(exerciseDataList: List<Triple<String, String, Exercise>>) {
        // Group data by date
        val groupedData = exerciseDataList.groupBy { it.second } // Group by date

        // Prepare data for adapter
        val adapterData = mutableListOf<Any>()
        adapterData.add("HeaderRow") // Add a static header row

        for ((date, exercises) in groupedData) {
            adapterData.add(date) // Add the date header
            for ((exerciseName, _, exercise) in exercises) {
                adapterData.add(
                    ExerciseData(
                        exerciseName = exerciseName,
                        minAngle = exercise.minAngle,
                        maxAngle = exercise.maxAngle,
                        reps = exercise.successfulReps,
                        time = exercise.time
                    )
                )
            }
        }

        // Initialize and set the adapter
        weeklyReportAdapter = WeeklyReportAdapter(adapterData)
        binding.recyclerViewWeeklyReport.adapter = weeklyReportAdapter
    }
}