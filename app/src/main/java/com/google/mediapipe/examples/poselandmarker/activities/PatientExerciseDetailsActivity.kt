package com.google.mediapipe.examples.poselandmarker.activities

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.examples.poselandmarker.ExerciseDetailsAdapter
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityPatientExerciseDetailsBinding
import com.google.mediapipe.examples.poselandmarker.model.Exercise
import com.google.mediapipe.examples.poselandmarker.utils.Constants

class PatientExerciseDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientExerciseDetailsBinding
    private lateinit var exerciseDetailsAdapter: ExerciseDetailsAdapter
    private lateinit var firestore: FirebaseFirestore
    private val exerciseListWithNames = mutableListOf<Pair<String, Exercise>>() // Pair of document name and exercise data

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the layout and hide the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        binding = ActivityPatientExerciseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(this)

        // Load exercises from Firestore
        loadExerciseData()
        // Retrieve patient details from intent
        val patientName = intent.getStringExtra("PATIENT_NAME")
        val patientImage = intent.getStringExtra("PATIENT_IMAGE")

        // Call setupActionBar with patient details
        setupActionBar(patientName, patientImage)

    }

    private fun setupActionBar(patientName: String?, patientImage: String?) {
        // Set the toolbar
        setSupportActionBar(binding.appBarMainExerciseDetails.toolbarMainActivity)

        // Clear default toolbar title
        supportActionBar?.title = ""

        // Create a LinearLayout to hold the navigation icon, patient image, and name
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL // Center vertically
        }

        // Set the navigation icon and its click listener
        val navigationIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_black_color_back_24dp)
            layoutParams = LinearLayout.LayoutParams(
                70, // Set width
                70  // Set height
            ).apply {
//                marginEnd = 16 // Space between icon and patient image
            }
            setOnClickListener {
                onBackPressed()
            }
        }

        // Create an ImageView programmatically for the patient's image
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                100, // Set width
                100  // Set height
            )
            scaleType = ImageView.ScaleType.CENTER_CROP

            // Load the patient's image using Glide with circular transformation
            if (patientImage != null) {
                Glide.with(this@PatientExerciseDetailsActivity)
                    .load(patientImage)
                    .placeholder(R.drawable.ic_user_place_holder) // Add a placeholder
                    .circleCrop() // Use circleCrop for circular image
                    .into(this)
            } else {
                setImageResource(R.drawable.ic_user_place_holder) // Set a default image
            }
        }

        // Create a TextView programmatically for the patient's name
        val textView = TextView(this).apply {
            text = patientName ?: "Patient"
            setTextColor(ContextCompat.getColor(this@PatientExerciseDetailsActivity, android.R.color.white))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 36 // Space between image and text
            }
        }

        // Add views to the linear layout
        linearLayout.addView(navigationIcon)
        linearLayout.addView(imageView)
        linearLayout.addView(textView)

        // Add the linear layout to the toolbar
        binding.appBarMainExerciseDetails.toolbarMainActivity.addView(linearLayout)
    }





    private fun loadExerciseData() {
        // Retrieve the passed patient ID from the intent
        val patientId = intent.getStringExtra("PATIENT_ID")

        if (patientId != null) {
            // Fetch exercises from the specific patient's document
            firestore.collection(Constants.PATIENTUSERS)
                .document(patientId)  // Use patientId here instead of userID
                .collection(Constants.EXERCISE)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val documentName = document.id
                        val exercise = document.toObject(Exercise::class.java)

                        if (exercise != null) {
                            Log.d("ExerciseData", "Document: $documentName, Exercise: $exercise")
                            exerciseListWithNames.add(Pair(documentName, exercise))
                        }
                    }

                    if (exerciseListWithNames.isEmpty()) {
                        Log.d("ExerciseData", "No exercises found.")
                    }

                    // Initialize the adapter and pass the exercise list with names
                    exerciseDetailsAdapter = ExerciseDetailsAdapter(exerciseListWithNames)
                    binding.recyclerViewExercises.adapter = exerciseDetailsAdapter
                }
                .addOnFailureListener { e ->
                    Log.e("PatientExerciseDetails", "Error loading exercises", e)
                }
        } else {
            Log.e("PatientExerciseDetails", "No patient ID found in intent")
        }
    }


}
