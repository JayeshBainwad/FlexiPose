package com.google.mediapipe.examples.poselandmarker.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.examples.poselandmarker.MainViewModel
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null
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
        val navHostFragment = supportFragmentManager.findFragmentById(binding!!.fragmentContainer.id) as NavHostFragment
        val navController = navHostFragment.navController
        binding?.navigation?.setupWithNavController(navController)
        binding?.navigation?.setOnNavigationItemReselectedListener {
            // Ignore reselection
        }

        // Set up the toolbar and navigation drawer
        setupActionBar()
        binding?.navHeaderMain?.setNavigationItemSelectedListener(this)

        // Get the current logged in user details.
        FirestoreClass().loadUserDetails(this@MainActivity)
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
    fun updateNavigationUserDetails(user: User) {
        // The instance of the header view of the navigation view.
        val headerView = binding?.navHeaderMain?.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView?.findViewById<ImageView>(R.id.iv_user_image)

        // Load the user image in the ImageView.
        if (navUserImage != null) {
            Glide
                .with(this@MainActivity)
                .load(user.image) // URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                .into(navUserImage)
        } // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView?.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        navUsername?.text = user.name
        Log.d("UserName:","$navUsername")
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                Toast.makeText(this@MainActivity, "My Profile", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MyProfileActivity::class.java)



                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            R.id.nav_sign_out -> {
                // Sign out the user from Firebase
                FirebaseAuth.getInstance().signOut()

                // Send the user to the sign-in screen
                val intent = Intent(this, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
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
}
