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

        if (patientId != null) {
            val exerciseDataList = mutableListOf<Triple<String, String, Exercise>>() // Triple<Exercise Name, Date, Exercise Details>
            val exerciseNames = listOf("ElbowExercise", "KneeExercise", "ShoulderExercise")

            var fetchCount = 0
            for (exerciseName in exerciseNames) {
                firestore.collection(Constants.PATIENTUSERS)
                    .document(patientId)
                    .collection(exerciseName)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val exercise = document.toObject(Exercise::class.java)
                            if (exercise != null) {
                                exerciseDataList.add(Triple(exerciseName, exercise.date, exercise))
                            }
                        }

                        fetchCount++
                        if (fetchCount == exerciseNames.size) {
                            processAndSetData(exerciseDataList)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("loadWeeklyData", "Error fetching exercises from $exerciseName", e)
                        fetchCount++
                        if (fetchCount == exerciseNames.size) {
                            processAndSetData(exerciseDataList)
                        }
                    }
            }
        } else {
            Log.e("loadWeeklyData", "Patient ID is null.")
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