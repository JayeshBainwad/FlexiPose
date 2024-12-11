package com.google.mediapipe.examples.poselandmarker.activities.doctor

import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mediapipe.examples.poselandmarker.PatientListAdapter
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.activities.BaseActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityDoctorMainBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Doctor
import com.google.mediapipe.examples.poselandmarker.model.Patient
import com.google.mediapipe.examples.poselandmarker.utils.Constants

class DoctorMainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityDoctorMainBinding? = null
    private lateinit var patientListAdapter: PatientListAdapter
    private var patientList: ArrayList<Patient> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the layout and hide the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Initialize View Binding
        binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set nav_patient_list visibility to VISIBLE
        // Note: You might need to adjust this part depending on how you handle the navigation menu
        val navView: NavigationView = findViewById(R.id.nav_header_main) // Replace with your NavigationView ID
        val menu: Menu = navView.menu
        val patientListItem = menu.findItem(R.id.nav_patient_list)
        patientListItem.isVisible = true // or set to false to hide

        // Set up the toolbar and navigation drawer
        setupActionBar()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        binding?.navHeaderMain?.setNavigationItemSelectedListener(this)

        // Call modular functions
        setupRecyclerView()
        setupSearchView()

        // Load doctor details into the UI
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserDoctorDetails(this@DoctorMainActivity)
        hideProgressDialog()
    }

    private fun setupRecyclerView() {
        binding?.rvPatientList?.layoutManager = LinearLayoutManager(this)

        // Pass a lambda function for handling patient item clicks and add button clicks
        patientListAdapter = PatientListAdapter(patientList, { selectedPatient ->
            // Navigate to the PatientExerciseDetailsActivity and pass patient details
            val intent = Intent(this@DoctorMainActivity, PatientExerciseDetailsActivity::class.java)
            intent.putExtra("PATIENT_ID", selectedPatient.id) // Assuming there's a 'id' field in Patient
            intent.putExtra("PATIENT_NAME", selectedPatient.name)
            intent.putExtra("PATIENT_IMAGE", selectedPatient.image)
            startActivity(intent)
        }, { patientToAdd ->
            // Handle the add button click here (e.g., add the patient to saved patients)
            addPatient(patientToAdd) // Call your add patient function here
        })

        binding?.rvPatientList?.adapter = patientListAdapter
    }


    /**
     * Function to add a patient name to Firestore.
     */
    private fun addPatient(patient: Patient) {
        val db = FirebaseFirestore.getInstance()
        val currentDoctorId = FirestoreClass().getCurrentUserID() // Assuming this function exists

        // Add the patient to the "Saved Patients" collection for the current doctor
        db.collection(Constants.DOCTORUSERS)
            .document(currentDoctorId)
            .collection("Saved Patients")
            .document(patient.id) // Assuming 'id' is unique
            .set(patient) // Save the patient object
            .addOnSuccessListener {
                Toast.makeText(this, "Patient added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("DoctorMainActivity", "Error adding patient", e)
                Toast.makeText(this, "Error adding patient", Toast.LENGTH_SHORT).show()
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
                    clearPatientList()
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


    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMainDoctor?.toolbarMainActivity)
        binding?.appBarMainDoctor?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_drawer_navigation_menu)
        binding?.appBarMainDoctor?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this@DoctorMainActivity, DoctorProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@DoctorMainActivity, DoctorSignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                this.finish()
            }
            R.id.nav_patient_list -> {
                // Navigate to SavedPatientListActivity to display saved patients
                val intent = Intent(this@DoctorMainActivity, SavedPatientListActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(doctor: Doctor) {
        val headerView = binding?.navHeaderMain?.getHeaderView(0)
        val navUserImage = headerView?.findViewById<ImageView>(R.id.iv_user_image)
        val username = headerView?.findViewById<TextView>(R.id.tv_username)

        username?.text = doctor.name
        if (navUserImage != null) {
            Glide
                .with(this)
                .load(doctor.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(navUserImage)
        }
    }

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
