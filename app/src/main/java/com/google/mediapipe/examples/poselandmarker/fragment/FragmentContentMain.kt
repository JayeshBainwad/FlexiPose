package com.google.mediapipe.examples.poselandmarker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentContentMainBinding

class FragmentContentMain: Fragment(), PoseLandmarkerHelper.LandmarkerListener {

    private var _fragmentContentMainBinding: FragmentContentMainBinding? = null
    private val fragmentContentMainBinding get() = _fragmentContentMainBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentContentMainBinding = FragmentContentMainBinding.inflate(inflater, container, false)
        return fragmentContentMainBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listeners for each card
        fragmentContentMainBinding.cardElbowExercise.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentContentMain_to_exerciseCameraActivity)
//            fragmentContentMainBinding.cardKneeExercise.setOnClickListener {
//                startActivity(Intent(this,ExerciseCameraActivity::class.java))
//            }
        }

        fragmentContentMainBinding.cardKneeExercise.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentContentMain_to_exerciseCameraActivity)
        }

        fragmentContentMainBinding.cardShoulderExercise.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentContentMain_to_exerciseCameraActivity)
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onError(error: String, errorCode: Int) {
        // Implement error handling
    }

    override fun onResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        // Implement result handling
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentContentMainBinding = null // Clear the binding when view is destroyed
    }
}
