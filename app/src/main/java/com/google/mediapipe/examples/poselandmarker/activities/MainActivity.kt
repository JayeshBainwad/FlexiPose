package com.google.mediapipe.examples.poselandmarker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.examples.poselandmarker.MainViewModel
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityCameraBinding
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Patient

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null

    // Constants for permission request codes
    private val CAMERA_PERMISSION_CODE = 100
    private val READ_MEDIA_PERMISSION_CODE = 101

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // Ensures the content extends to the area around the notch
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set up the toolbar and navigation drawer
        setupActionBar()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        binding?.navHeaderMain?.setNavigationItemSelectedListener(this)

        // Get the current logged in user details.
        FirestoreClass().loadUserDetails(this@MainActivity)

        // Set permission checks on card click
        binding?.cardElbowExercise?.setOnClickListener {
            checkAndRequestPermissions(
                permissions = arrayOf(Manifest.permission.CAMERA),
                requestCode = CAMERA_PERMISSION_CODE
            ) {
                // Permission granted - Start CameraActivity for Elbow Exercise
                val intent = Intent(this@MainActivity, CameraActivity::class.java)
                intent.putExtra("exerciseType", "Elbow")
                startActivity(intent)
            }
        }

        binding?.cardKneeExercise?.setOnClickListener {
            checkAndRequestPermissions(
                permissions = arrayOf(Manifest.permission.CAMERA),
                requestCode = CAMERA_PERMISSION_CODE
            ) {
                // Permission granted - Start CameraActivity for Knee Exercise
                val intent = Intent(this@MainActivity, CameraActivity::class.java)
                intent.putExtra("exerciseType", "Knee")
                startActivity(intent)
            }
        }

        binding?.cardShoulderExercise?.setOnClickListener {
            checkAndRequestPermissions(
                permissions = arrayOf(Manifest.permission.CAMERA),
                requestCode = CAMERA_PERMISSION_CODE
            ) {
                // Permission granted - Start CameraActivity for Knee Exercise
                val intent = Intent(this@MainActivity, CameraActivity::class.java)
                intent.putExtra("exerciseType", "Shoulder")
                startActivity(intent)
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMain?.toolbarMainActivity)
        binding?.appBarMain?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_drawer_navigation_menu)
        binding?.appBarMain?.toolbarMainActivity?.setNavigationOnClickListener {
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

    // Function to check and request permissions
    private fun checkAndRequestPermissions(permissions: Array<String>, requestCode: Int, onGranted: () -> Unit) {
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            // All permissions are already granted
            onGranted()
        } else {
            // Request missing permissions
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Camera permission granted, you can start the exercise here
                    // For simplicity, you can use a separate logic for camera-related activities.
                } else {
                    // Permission denied, show a message to the user
                    Log.e("Permissions", "Camera permission was denied.")
                }
            }
            READ_MEDIA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Media permission granted, handle accordingly
                } else {
                    Log.e("Permissions", "Read media permission was denied.")
                }
            }
        }
    }

    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(patient: Patient) {
        val headerView = binding?.navHeaderMain?.getHeaderView(0)
        val navUserImage = headerView?.findViewById<ImageView>(R.id.iv_user_image)
        if (navUserImage != null) {
            Glide.with(this@MainActivity)
                .load(patient.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(navUserImage)
        }
        val navUsername = headerView?.findViewById<TextView>(R.id.tv_username)
        navUsername?.text = patient.name
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                this.finish()
            }
        }
        return true
    }

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
