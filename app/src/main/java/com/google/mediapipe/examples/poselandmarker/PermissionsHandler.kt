package com.google.mediapipe.examples.poselandmarker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_MEDIA_IMAGES,
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_NETWORK_STATE
)

class PermissionsHandler(
    private val context: Context,
    private val navController: NavController,
    private val lifecycleOwner: LifecycleOwner,
    private val requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermissions() {
        if (hasPermissions(context)) {
            navigateToHome()
        } else {
            requestPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissions() {
        requestMultiplePermissionsLauncher.launch(PERMISSIONS_REQUIRED)
    }

    private fun navigateToHome() {
        lifecycleOwner.lifecycleScope.launchWhenStarted {
//            navController.navigate(R.id.action_permissions_fragment_to_mainActivity)
        }
    }

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
