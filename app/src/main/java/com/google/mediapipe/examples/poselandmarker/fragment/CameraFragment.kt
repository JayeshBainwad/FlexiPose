/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.poselandmarker.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.appbar.AppBarLayout
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper
import com.google.mediapipe.examples.poselandmarker.MainViewModel
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener {

    companion object {
        private const val TAG = "PoseLandmarker"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding get() = _fragmentCameraBinding!!

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK

    private lateinit var backgroundExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the AppBarLayout
//        val appBarLayout: AppBarLayout? = activity?.findViewById(R.id.toolbar_main_activity)
//        appBarLayout?.visibility = View.GONE

        // Hide status bars and extend content into the notch area
//        activity?.let {
//            WindowCompat.setDecorFitsSystemWindows(it.window, false)
//            val controller = WindowInsetsControllerCompat(it.window, it.window.decorView)
//            controller.hide(WindowInsetsCompat.Type.statusBars())
//
//            // Ensure content is drawn around the notch (cutout) area
//            it.window.attributes.layoutInDisplayCutoutMode =
//                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
//        }

        backgroundExecutor = Executors.newSingleThreadExecutor()
        fragmentCameraBinding.viewFinder.post {
            setUpCamera()
        }

        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = viewModel.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = viewModel.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = viewModel.currentMinPosePresenceConfidence,
//                currentDelegate = viewModel.currentDelegate,
                poseLandmarkerHelperListener = this
            )
        }

//        initBottomSheetControls()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
//        if (!PermissionsFragment.hasPermissions(requireContext())) {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container)
//                .navigate(R.id.action_camera_to_permissions)
//        }

        backgroundExecutor.execute {
            if (this::poseLandmarkerHelper.isInitialized && poseLandmarkerHelper.isClose()) {
                poseLandmarkerHelper.setupPoseLandmarker()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::poseLandmarkerHelper.isInitialized) {
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
//            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate)
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    detectPose(image)
                }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if (this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
//                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
//                    String.format("%d ms", resultBundle.inferenceTime)

                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
//            if (errorCode == PoseLandmarkerHelper.GPU_ERROR) {
//                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
//                    PoseLandmarkerHelper.DELEGATE_CPU, false
//                )
//            }
        }
    }

//    private fun initBottomSheetControls() {
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.text =
//            String.format(Locale.US, "%.2f", viewModel.currentMinPoseDetectionConfidence)
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.text =
//            String.format(Locale.US, "%.2f", viewModel.currentMinPoseTrackingConfidence)
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.text =
//            String.format(Locale.US, "%.2f", viewModel.currentMinPosePresenceConfidence)
//
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdMinus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseDetectionConfidence >= 0.2) {
//                poseLandmarkerHelper.minPoseDetectionConfidence -= 0.1f
//                updateControlsUi()
//            }
//        }
//
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdPlus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseDetectionConfidence <= 0.8) {
//                poseLandmarkerHelper.minPoseDetectionConfidence += 0.1f
//                updateControlsUi()
//            }
//        }
//
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdMinus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseTrackingConfidence >= 0.2) {
//                poseLandmarkerHelper.minPoseTrackingConfidence -= 0.1f
//                updateControlsUi()
//            }
//        }
//
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdPlus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseTrackingConfidence <= 0.8) {
//                poseLandmarkerHelper.minPoseTrackingConfidence += 0.1f
//                updateControlsUi()
//            }
//        }
//
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdMinus.setOnClickListener {
//            if (poseLandmarkerHelper.minPosePresenceConfidence >= 0.2) {
//                poseLandmarkerHelper.minPosePresenceConfidence -= 0.1f
//                updateControlsUi()
//            }
//        }
//
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdPlus.setOnClickListener {
//            if (poseLandmarkerHelper.minPosePresenceConfidence <= 0.8) {
//                poseLandmarkerHelper.minPosePresenceConfidence += 0.1f
//                updateControlsUi()
//            }
//        }
//
////        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(viewModel.currentDelegate, false)
////        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
////            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
////                try {
////                    poseLandmarkerHelper.currentDelegate = p2
////                } catch (e: Exception) {
////                    Log.e(TAG, "Error setting delegate", e)
////                }
////            }
////
////            override fun onNothingSelected(p0: AdapterView<*>?) {}
////        }
//    }

//    private fun updateControlsUi() {
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.text =
//            String.format(Locale.US, "%.2f", poseLandmarkerHelper.minPoseDetectionConfidence)
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.text =
//            String.format(Locale.US, "%.2f", poseLandmarkerHelper.minPoseTrackingConfidence)
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.text =
//            String.format(Locale.US, "%.2f", poseLandmarkerHelper.minPosePresenceConfidence)
//    }
}
