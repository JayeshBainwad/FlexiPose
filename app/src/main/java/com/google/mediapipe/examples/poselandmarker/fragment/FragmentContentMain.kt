package com.google.mediapipe.examples.poselandmarker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.transition.Visibility
import com.google.mediapipe.examples.poselandmarker.PoseLandmarkerHelper
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentContentMainBinding
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentHomeBinding

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
