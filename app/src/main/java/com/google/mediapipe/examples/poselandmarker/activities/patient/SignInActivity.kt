package com.google.mediapipe.examples.poselandmarker.activities.patient

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.activities.BaseActivity
import com.google.mediapipe.examples.poselandmarker.activities.IntroActivity
import com.google.mediapipe.examples.poselandmarker.databinding.ActivitySignInBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.Patient

// TODO (Step 1: Extend the BaseActivity instead of AppCompatActivity.)
class SignInActivity : BaseActivity() {

    private var binding: ActivitySignInBinding? = null
//    private var autoLoggedIn : Boolean = false
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {

        //This call the parent constructor
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // This line ensures the content extends to the area around the notch
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        binding?.icNavToIntroPage?.setOnClickListener() {
            startActivity(Intent(this@SignInActivity, IntroActivity::class.java))
        }


//        if (!autoLoggedIn) {
//            // Adding the handler to after the a task after some delay.
//            Handler().postDelayed({
//
//                // Here if the user is signed in once and not signed out again from the app. So next time while coming into the app
//                // we will redirect him to MainScreen or else to the Intro Screen as it was before.
//
//
//                // Get the current user id
//                val currentUserID = FirestoreClass().getCurrentUserID()
//                // Start the Intro Activity
//
//                if (currentUserID.isNotEmpty()) {
//                    autoLoggedIn = true
//                    // Start the Main Activity
//                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
//                } else {
//                    // Start the Intro Activity
//                    startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
//                }
//                hideProgressDialog()
//                finish() // Call this when your activity is done and should be closed.
//            }, 2000)
//
        // TODO(Step 4: Add click event for sign-in button and call the function to sign in.)
        // START
        binding?.btnSignIn?.setOnClickListener {
            signInRegisteredUser()
        }

        binding?.tvCreateAccount?.setOnClickListener {
            intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        // END
    }

    /**
     * A function for actionBar Setup.
     */
//    private fun setupActionBar() {
//
//        setSupportActionBar(binding?.toolbarSignInActivity)
//
//        val actionBar = supportActionBar
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
//        }
//
//        binding?.toolbarSignInActivity?.setNavigationOnClickListener {
//            startActivity(Intent(this@SignInActivity,IntroActivity::class.java))
//        }
//    }

    // TODO (Step 2: A function for Sign-In using the registered user using the email and password.)
    // START
    /**
     * A function for Sign-In using the registered user using the email and password.
     */
    private fun signInRegisteredUser() {
        // Here we get the text from editText and trim the space
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Sign-In using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // TODO (Step 2: Remove the toast message and call the FirestoreClass signInUser function to get the data of user from database. And also move the code of hiding Progress Dialog and Launching MainActivity to Success function.)
                        // Calling the FirestoreClass signInUser function to get the data of user from database.
                        FirestoreClass().loadUserDetails(this@SignInActivity)
                        // END
                    } else {

                        // Hide the progress dialog
                        hideProgressDialog()

                        Toast.makeText(
                            this@SignInActivity,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
    // END

    // TODO (Step 3: A function to validate the entries of a user.)
    // START
    /**
     * A function to validate the entries of a user.
     */
    private fun validateForm(email: String, password: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            showErrorSnackBar("Please enter email.")
            false
        } else if (TextUtils.isEmpty(password)) {
            showErrorSnackBar("Please enter password.")
            false
        } else {
            true
        }
    }
    // END

    /**
     * A function to get the user details from the firestore database after authentication.
     */
    fun signInSuccess(patient: Patient) {

        hideProgressDialog()

        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        this.finish()
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        doubleBackToExit()
//    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}