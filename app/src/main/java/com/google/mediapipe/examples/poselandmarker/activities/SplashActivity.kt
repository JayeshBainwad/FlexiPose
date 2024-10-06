package com.google.mediapipe.examples.poselandmarker.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.common.io.Resources
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivitySplashBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    /**
     * This function is auto created by Android when the Activity Class is created.
     */

    private var binding: ActivitySplashBinding? = null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // This line ensures the content extends to the area around the notch
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // This is used to align the xml view to this class
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        // This is used to get the file from the assets folder and set it to the title textView.
        val typeface: Typeface =
            Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding?.tvAppName?.typeface = typeface

        // Adding the handler to after the a task after some delay.
        Handler().postDelayed({

            // Here if the user is signed in once and not signed out again from the app. So next time while coming into the app
            // we will redirect him to MainScreen or else to the Intro Screen as it was before.

            // Get the current user ID
            val currentUserID = FirestoreClass().getCurrentUserID()

            if (currentUserID.isNotEmpty()) {
                // Query Firestore to get the user type
                FirestoreClass().getUserType(currentUserID) { userType ->
                    if (userType == "patient") {
                        // If user is a patient, navigate to MainActivity
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    } else if (userType == "doctor") {
                        // If user is a doctor, navigate to DoctorMainActivity
                        startActivity(Intent(this@SplashActivity, DoctorMainActivity::class.java))
                    } else {
                        // Handle case where userType is not found
                        Toast.makeText(this, "User type not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // If no user ID, navigate to the SignUpActivity
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
//                finish()
            }

//            if (currentUserID.isNotEmpty()) {
//                startActivity(Intent(this@SplashActivity, DoctorMainActivity::class.java))
//            }else{
//                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
//            }


//            hideProgressDialog()
            finish() // Call this when your activity is done and should be closed.
        }, 500) // Here we pass the delay time in milliSeconds after which the splash activity will disappear.
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}