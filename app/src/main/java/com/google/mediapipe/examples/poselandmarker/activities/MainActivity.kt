package com.google.mediapipe.examples.poselandmarker.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    private var binding2: ActivityCameraBinding? = null
    private val viewModel : MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // Ensures the content extends to the area around the notch
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Set up the navigation host fragment
//        val navHostFragment = supportFragmentManager.findFragmentById(binding!!.fragmentContainer.id) as NavHostFragment
//        val navController = navHostFragment.navController
//        binding?.navigation?.setupWithNavController(navController)
//        binding?.navigation?.setOnNavigationItemReselectedListener {
//            // Ignore reselection
//        }

        // Set up the toolbar and navigation drawer
        setupActionBar()
        binding?.navHeaderMain?.setNavigationItemSelectedListener(this)

        // Get the current logged in user details.
        FirestoreClass().loadUserDetails(this@MainActivity)

        binding?.cardElbowExercise?.setOnClickListener {
            val intent = Intent(this@MainActivity,CameraActivity::class.java)
            intent.putExtra("exerciseType","Elbow")
            startActivity(intent)
        }

        binding?.cardKneeExercise?.setOnClickListener {
            val intent = Intent(this@MainActivity,CameraActivity::class.java)
            intent.putExtra("exerciseType","Knee")
            startActivity(intent)
        }

    }

    private fun setupActionBar() {
        // Set the toolbar as the ActionBar using ViewBinding
        setSupportActionBar(binding?.appBarMain?.toolbarMainActivity)

        // Set the navigation icon for the drawer menu
        binding?.appBarMain?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_drawer_navigation_menu)

        // Set click listener to toggle the drawer
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

    /**
     * A function to get the current user details from firebase.
     */
    fun updateNavigationUserDetails(patient: Patient) {
        // The instance of the header view of the navigation view.
        val headerView = binding?.navHeaderMain?.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView?.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        if (navUserImage != null) {
            Glide
                .with(this@MainActivity)
                .load(patient.image) // URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                .into(navUserImage)
        } // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView?.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername?.text = patient.name
        Log.d("UserName:","$navUsername")
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
//                Toast.makeText(this@MainActivity, "My Profile", Toast.LENGTH_SHORT).show()
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
//                finish()
            }
            R.id.nav_sign_out -> {
                // Sign out the user from Firebase
                FirebaseAuth.getInstance().signOut()

                // Send the user to the sign-in screen
                val intent = Intent(this@MainActivity, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                this.finish()
            }
        }
//        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    // TODO (Step 4: Add the onActivityResult function and check the result of the activity for which we expect the result.)
    // START
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            // Get the user updated details.
            FirestoreClass().loadUserDetails(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }
    // END

    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            // A double back press function is added in Base Activity.
            doubleBackToExit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
