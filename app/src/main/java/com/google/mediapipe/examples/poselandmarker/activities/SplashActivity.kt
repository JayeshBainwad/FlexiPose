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

    private var binding: ActivitySplashBinding? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding?.tvAppName?.typeface = typeface

        Handler().postDelayed({
            val currentUserID = FirestoreClass().getCurrentUserID()

            if (currentUserID.isNotEmpty()) {
                FirestoreClass().getUserType(currentUserID) { userType ->
                    if (userType == "patient") {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    } else if (userType == "doctor") {
                        startActivity(Intent(this@SplashActivity, DoctorMainActivity::class.java))
                    } else {
                        // UserType not found, navigate to IntroActivity
                        startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                    }
                    finish() // Finish only after navigating to the proper activity
                }
            } else {
                // If the user is not logged in, navigate to SignUpActivity
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                finish()
            }

        }, 2000) // Delay before moving to the next screen
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}