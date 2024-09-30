package com.google.mediapipe.examples.poselandmarker.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.mediapipe.examples.poselandmarker.R

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val PERMISSIONS_REQUIRED = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_MEDIA_IMAGES,
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_NETWORK_STATE
)

class PermissionsFragment : Fragment() {

    // Register for the result of the permission request. This handles multiple permissions.
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allPermissionsGranted = true
            for (isGranted in permissions.values) {
                if (!isGranted) {
                    allPermissionsGranted = false
                    break
                }
            }

            if (allPermissionsGranted) {
                Toast.makeText(
                    context,
                    "All permissions granted",
                    Toast.LENGTH_LONG
                ).show()
                navigateToHome()
            } else {
                Toast.makeText(
                    context,
                    "One or more permissions denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if all permissions are already granted.
        if (hasPermissions(requireContext())) {
            navigateToHome()
        } else {
            // Request permissions if they are not already granted.
            requestMultiplePermissionsLauncher.launch(PERMISSIONS_REQUIRED)
        }
    }

    private fun navigateToHome() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(
                requireActivity(),
                R.id.fragment_container
            ).navigate(
                R.id.action_permissions_fragment_to_fragmentContentMain
            )
        }
    }

    companion object {

        /** Convenience method used to check if all permissions required by this app are granted */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
