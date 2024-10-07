package com.google.mediapipe.examples.poselandmarker.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityDoctorMainBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Doctor

class DoctorMainActivity : BaseActivity() {

    private lateinit var mDoctorDetails: Doctor
    private var binding: ActivityDoctorMainBinding? = null

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This is used to align the xml view to this class

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // This line ensures the content extends to the area around the notch
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Initialize View Binding
        binding = ActivityDoctorMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)  // Set the content view with the binding root

        val userId = FirestoreClass().getCurrentUserID()

        // Load doctor details into the UI
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserDoctorDetails(this@DoctorMainActivity)
        hideProgressDialog()

        binding?.icSignOut?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }
    }

    fun setDoctorDataInUI(doctor: Doctor) {
        mDoctorDetails = doctor

        // Load the doctor's profile image using Glide
        binding?.let {
            Glide.with(this@DoctorMainActivity)
                .load(doctor.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(it.ivDoctorProfileImage)

            it.tvDoctorName.text = doctor.name
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null  // Clear binding when activity is destroyed
    }
}
