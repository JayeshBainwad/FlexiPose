package com.google.mediapipe.examples.poselandmarker.activities

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.mediapipe.examples.poselandmarker.OverlayView
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityCameraBinding
import com.google.mediapipe.examples.poselandmarker.exercises.ElbowExercise
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity(), PoseLandmarkerHelper.LandmarkerListener {

    companion object {
        private const val TAG = "PoseLandmarker"
    }

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var canvas: Canvas

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    private var binding: ActivityCameraBinding? = null

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var cameraControl: CameraControl
    private var currentZoomRatio = 1f
    private var maxZoomRatio = 1f

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Hide status bars and extend content into the notch area
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        backgroundExecutor = Executors.newSingleThreadExecutor()
        binding?.viewFinder?.post {
            setUpCamera()
        }

        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = this,
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = 0.5f,
                minPoseTrackingConfidence = 0.5f,
                minPosePresenceConfidence = 0.5f,
                poseLandmarkerHelperListener = this
            )
        }

        // Pass the binding object to the ElbowExercise
        binding?.elbowExercise?.setCameraActivityBinding(binding!!)
        binding?.kneeExercise?.setCameraActivityBinding(binding!!)
        binding?.shoulderExercise?.setCameraActivityBinding(binding!!)
        binding?.kneeExercise?.visibility = View.INVISIBLE
        binding?.elbowExercise?.visibility = View.INVISIBLE
        binding?.shoulderExercise?.visibility = View.INVISIBLE

        val exerciseType = intent.getStringExtra("exerciseType")
        if (exerciseType != null) {
            Log.d("exerciseTypeAfterNavigation", exerciseType)
        } else {
            Log.d("exerciseTypeAfterNavigation", "No exercise type found")
        }

        // Apply visibility based on the exercise type
        when (exerciseType) {
            "Elbow" -> {
                binding?.elbowExercise?.visibility = View.VISIBLE
//                binding?.kneeExercise?.isGone = true
            }
            "Knee" -> {
//                binding?.elbowExercise?.isGone = true
                binding?.kneeExercise?.visibility = View.VISIBLE
            }
            "Shoulder" -> {
//                binding?.elbowExercise?.isGone = true
                binding?.shoulderExercise?.visibility = View.VISIBLE
            }
            else -> {
                Log.e("exerciseTypeAfterNavigation", "Unknown exercise type: $exerciseType")
            }
        }

        // Set up the scale gesture detector for zoom functionality
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor
                val newZoomRatio = currentZoomRatio * scale
                // Clamp zoom to the maximum and minimum values
                cameraControl.setZoomRatio(newZoomRatio.coerceIn(1f, maxZoomRatio))
                currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                return true
            }
        })

        // Detect pinch gestures on the preview
        binding?.viewFinder?.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        backgroundExecutor.execute {
            if (this::poseLandmarkerHelper.isInitialized && poseLandmarkerHelper.isClose()) {
                poseLandmarkerHelper.setupPoseLandmarker()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::poseLandmarkerHelper.isInitialized) {
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Set up Preview use case
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding?.viewFinder?.display?.rotation!!)
            .build()

        // Set up ImageAnalysis use case
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding?.viewFinder?.display?.rotation!!)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    detectPose(image)
                }
            }

        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        try {
            // Bind camera to lifecycle with Preview and ImageAnalysis use cases
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            preview?.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)

            cameraControl = camera?.cameraControl!!
            maxZoomRatio = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
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
        imageAnalyzer?.targetRotation = binding?.viewFinder?.display?.rotation!!
    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        val exerciseType = intent.getStringExtra("exerciseType")
        runOnUiThread {

            // Only update the visible exercise
            when (exerciseType) {
                "Elbow" -> {
                    binding?.elbowExercise?.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM
                    )
                    binding?.elbowExercise?.invalidate()
                }
                "Knee" -> {
                    binding?.kneeExercise?.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM
                    )
                    binding?.kneeExercise?.invalidate()
                }
                "Shoulder" -> {
                    binding?.shoulderExercise?.setResults(
                        resultBundle.results.first(),
                        resultBundle.inputImageHeight,
                        resultBundle.inputImageWidth,
                        RunningMode.LIVE_STREAM
                    )
                    binding?.shoulderExercise?.invalidate()
                }
                else -> {
                    Log.e("exerciseTypeAfterNavigation", "Unknown exercise type: $exerciseType")
                }
            }

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Pass touch event to scale gesture detector
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}
