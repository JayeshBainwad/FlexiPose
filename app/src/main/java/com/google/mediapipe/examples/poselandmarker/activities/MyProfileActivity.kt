@file:Suppress("DEPRECATION")

package com.google.mediapipe.examples.poselandmarker.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMyProfileBinding
import com.google.mediapipe.examples.poselandmarker.firebase.FirestoreClass
import com.google.mediapipe.examples.poselandmarker.model.User
import com.google.mediapipe.examples.poselandmarker.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var binding: ActivityMyProfileBinding? = null
    private var mSelectedImageFileUri: Uri? = null

    // A global variable for user details.
    private lateinit var mUserDetails: User

    // A global variable for a user profile image URL
    private var mProfileImageURL: String = ""

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())

        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        FirestoreClass().loadUserDetails(this@MyProfileActivity)

        setupActionBar()

        binding?.ivProfileUserImage?.setOnClickListener {
            // Call the file chooser function without permission check
            showFileChooser()
        }

        binding?.btnUpdate?.setOnClickListener {
            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {

                uploadUserImage()
            } else {

                showProgressDialog(resources.getString(R.string.please_wait))

                // Call a function to update user details in the database.
                updateUserProfileData()
            }
        }
    }

    private fun showFileChooser() {
        val fileChooserIntent = Intent(Intent.ACTION_GET_CONTENT)
        fileChooserIntent.type = "*/*" // Select any file type
        startActivityForResult(fileChooserIntent, PICK_FILE_REQUEST_CODE)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_FILE_REQUEST_CODE && data?.data != null) {
            mSelectedImageFileUri = data.data
            try {
                binding?.ivProfileUserImage?.let {
                    Glide.with(this@MyProfileActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val PICK_FILE_REQUEST_CODE = 2 // Changed constant for file picking
    }

    private fun getFileExtension(uri: Uri?) : String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    /**
     * A function to upload the selected user image to firebase cloud storage.
     */
    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(
                    mSelectedImageFileUri
                )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            mProfileImageURL = uri.toString()
                            hideProgressDialog()

                            // Call a function to update user details in the database.
                            updateUserProfileData()
                        }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }

    /**
     * A function to update the user profile details into the database.
     */
    private fun updateUserProfileData() {

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (binding?.etName?.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
        }

        if (binding?.etMobile?.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
        }

        // Update the data in the database.
        FirestoreClass().updateUserProfileData(this@MyProfileActivity, userHashMap)
    }

    /**
     * A function to notify the user profile is updated successfully.
     */
    fun profileUpdateSuccess() {

        hideProgressDialog()

        // TODO (Step 3: Send the success result to the Base Activity.)
        // START
        setResult(Activity.RESULT_OK)
        // END
        startActivity(Intent(this@MyProfileActivity, MainActivity::class.java))
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }
        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserDataInUI(user: User) {

        // Initialize the user details variable
        mUserDetails = user

        Glide.with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding?.ivProfileUserImage!!)

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if (user.mobile != 0L) {
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
