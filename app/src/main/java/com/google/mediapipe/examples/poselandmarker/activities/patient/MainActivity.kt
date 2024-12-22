/*
 * Copyright 2024 Jayesh Shivaji Bainwad.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.poselandmarker.activities.patient

import com.google.mediapipe.examples.poselandmarker.ExerciseAdapter
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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.activities.BaseActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Patient

data class ExerciseType(val name: String)

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null

    // Constants for permission request codes
    private val CAMERA_PERMISSION_CODE = 100
    private val READ_MEDIA_PERMISSION_CODE = 101

    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exerciseTypeList = listOf(
        ExerciseType("Elbow exercise"),
        ExerciseType("Knee exercise"),
        ExerciseType("Shoulder exercise")
        // Add future exercises here up to 16 total
    )

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

        // Setup RecyclerView
        setupRecyclerView()

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        binding?.navHeaderMain?.setNavigationItemSelectedListener(this)

        // Get the current logged in user details.
        FirestoreClass().loadUserDetails(this@MainActivity)

        // Set permission checks on card click
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(this,exerciseTypeList) { exercise ->
            checkAndRequestPermissions(
                permissions = arrayOf(Manifest.permission.CAMERA),
                requestCode = CAMERA_PERMISSION_CODE
            ) {
                val intent = Intent(this@MainActivity, CameraActivity::class.java)
                intent.putExtra("exerciseType", exercise.name)
                startActivity(intent)
            }
        }

        binding?.recyclerViewExercises?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = exerciseAdapter
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMainPatient?.toolbarMainActivity)
        binding?.appBarMainPatient?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_drawer_navigation_menu)
        binding?.appBarMainPatient?.toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    fun updateAppbarTile(patient: Patient) {
        supportActionBar?.title = patient.name
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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
