package com.google.mediapipe.examples.poselandmarker.activities.doctor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.examples.poselandmarker.PatientListAdapter
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.SavedPatientListAdapter
import com.google.mediapipe.examples.poselandmarker.activities.BaseActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ActivitySavedPatientListBinding
import com.google.mediapipe.examples.poselandmarker.databinding.ItemPatientCardBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Patient
import com.google.mediapipe.examples.poselandmarker.utils.Constants

class SavedPatientListActivity : BaseActivity() {

    private var binding: ActivitySavedPatientListBinding? = null
    private lateinit var patientListAdapter: SavedPatientListAdapter
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

        setupSearchView()

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
        supportActionBar?.title = "Saved Patient List"
    }

    private fun setupRecyclerView() {
        binding?.rvSavedPatientList?.layoutManager = LinearLayoutManager(this)
        patientListAdapter = SavedPatientListAdapter(patientList, { selectedPatient ->
            // Navigate to the PatientExerciseDetailsActivity and pass patient details
            val intent = Intent(this@SavedPatientListActivity, PatientExerciseDetailsActivity::class.java)
            intent.putExtra("PATIENT_ID", selectedPatient.id) // Assuming there's a 'id' field in Patient
            intent.putExtra("PATIENT_NAME", selectedPatient.name)
            intent.putExtra("PATIENT_IMAGE", selectedPatient.image)
            startActivity(intent)
        }, { patientToRemove ->
            // Handle the add button click here (e.g., add the patient to saved patients)
            removePatient(patientToRemove) // Call your add patient function here
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

    /**
     * Function to search patients from Firestore based on the query.
     */
    private fun searchPatients(query: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection(Constants.PATIENTUSERS)
            .orderBy("name")
            .startAt(query)
            .endAt(query + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val newPatientList = ArrayList<Patient>()
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val patient = document.toObject(Patient::class.java)
                        newPatientList.add(patient)
                    }
                }
                // Use the updateList method to refresh the RecyclerView data
                patientListAdapter.updateList(newPatientList)
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    private fun setupSearchView() {
        val searchView = binding?.searchView
        searchView?.setIconifiedByDefault(false)
        binding?.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchPatients(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // If search text is empty, clear the patient list
//                    clearPatientList()
                    fetchSavedPatients()
                } else {
                    // If there is text, search for patients
                    searchPatients(newText)
                }
                return false
            }

        })
    }

    private fun clearPatientList() {
        // Clear the patient list in the adapter by passing an empty list
        patientListAdapter.updateList(ArrayList())
    }

    /**
     * Function to remove a patient from the Firestore database.
     */
    private fun removePatient(patient: Patient) {
        val db = FirebaseFirestore.getInstance()
        val currentDoctorId = FirestoreClass().getCurrentUserID() // Assuming this function exists

        // Remove the patient from the "Saved Patients" collection for the current doctor
        db.collection(Constants.DOCTORUSERS)
            .document(currentDoctorId)
            .collection("Saved Patients")
            .document(patient.id) // Assuming 'id' is unique and matches the document ID
            .delete() // Deletes the patient document
            .addOnSuccessListener {
                Toast.makeText(this, "Patient removed successfully", Toast.LENGTH_SHORT).show()
                // Optionally refresh the list or remove the item from the adapter
                patientListAdapter.removePatient(patient) // Call a method to remove from the list and update UI
            }
            .addOnFailureListener { e ->
                Log.e("DoctorMainActivity", "Error removing patient", e)
                Toast.makeText(this, "Error removing patient", Toast.LENGTH_SHORT).show()
            }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val searchView = binding?.searchView

        // If search view is focused and user touches outside, clear focus
        if (searchView?.hasFocus() == true) {
            val outRect = Rect()
            searchView.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                searchView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }


}