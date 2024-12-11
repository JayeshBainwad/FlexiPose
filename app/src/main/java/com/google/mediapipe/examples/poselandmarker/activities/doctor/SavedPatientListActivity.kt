package com.google.mediapipe.examples.poselandmarker.activities.doctor

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.examples.poselandmarker.PatientListAdapter
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.activities.BaseActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ActivitySavedPatientListBinding
import com.google.mediapipe.examples.poselandmarker.databinding.ItemPatientCardBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Patient
import com.google.mediapipe.examples.poselandmarker.utils.Constants

class SavedPatientListActivity : BaseActivity() {

    private var binding: ActivitySavedPatientListBinding? = null
    private lateinit var patientListAdapter: PatientListAdapter
    private var patientList: ArrayList<Patient> = ArrayList()
    private var binding2: ItemPatientCardBinding? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up the layout and hide the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Initialize View Binding
        binding = ActivitySavedPatientListBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        val addPatientButton: ImageButton = findViewById(R.id.btn_add_patient)
//        addPatientButton.isGone

        // Set up the toolbar and navigation drawer
        setupActionBar()

        // Call modular functions
        setupRecyclerView()
//        setupSearchView()
        fetchSavedPatients()

        // Load doctor details into the UI
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserDoctorDetails(this@SavedPatientListActivity)
        hideProgressDialog()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMainDoctorSavedPatient?.toolbarMainActivity)
        binding?.appBarMainDoctorSavedPatient?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_black_color_back_24dp)
        binding?.appBarMainDoctorSavedPatient?.toolbarMainActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding?.rvSavedPatientList?.layoutManager = LinearLayoutManager(this)
        patientListAdapter = PatientListAdapter(patientList, { selectedPatient ->
            // Handle patient item click
        }, { patientToAdd ->
            // Handle add patient click if needed
        })
        binding?.rvSavedPatientList?.adapter = patientListAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchSavedPatients() {
        val db = FirebaseFirestore.getInstance()
        val currentDoctorId = FirestoreClass().getCurrentUserID() // Assuming this function exists

        db.collection(Constants.DOCTORUSERS)
            .document(currentDoctorId)
            .collection("Saved Patients")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val patient = document.toObject(Patient::class.java)
                    patientList.add(patient)
                }
                patientListAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener { e ->
                Log.e("SavedPatientListActivity", "Error fetching saved patients", e)
                Toast.makeText(this, "Error fetching saved patients", Toast.LENGTH_SHORT).show()
            }
    }
}