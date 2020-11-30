package com.yektasarioglu.imagesegmentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment

import com.esafirm.imagepicker.features.ImagePicker

import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.yektasarioglu.imagesegmentation.TAG
import com.yektasarioglu.imagesegmentation.databinding.FragmentStreamBinding
import com.yektasarioglu.imagesegmentation.huawei.ImageSegmentAnalyzerTransactor

import java.io.IOException

private const val FPS = 20.0f
private const val WIDTH = 1280
private const val HEIGHT = 720

class StreamHumanBodySegmentationFragment : Fragment() {

    private var binding: FragmentStreamBinding? = null

    private var analyzer: MLImageSegmentationAnalyzer? = null
    private var lensEngine: LensEngine? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentStreamBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAnalyzer()
        initializeLensEngine()

        binding?.pickImageFromGalleryButton?.setOnClickListener { askImageFromUser() }
    }

    override fun onResume() {
        super.onResume()
        openCameraStream(lensEngine ?: return)
    }

    override fun onPause() {
        super.onPause()
        closeCameraStream()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseDetectionResources()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            binding?.apply {
                backgroundImageView.setImageURI(image.uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun askImageFromUser() = ImagePicker.create(this).single().start()

    private fun initializeAnalyzer() {
        val setting =
            MLImageSegmentationSetting.Factory() // Set whether to support fine segmentation. The value true indicates fine segmentation, and the value false indicates fast segmentation.
                .setExact(false)
                .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
                .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
                .create()
        analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting)

        analyzer?.setTransactor(ImageSegmentAnalyzerTransactor(binding?.graphic ?: return))
    }

    private fun initializeLensEngine() {
        lensEngine = LensEngine.Creator(activity, analyzer)
                .setLensType(LensEngine.FRONT_LENS)
                .applyDisplayDimension(WIDTH, HEIGHT)
                .applyFps(FPS)
                .enableAutomaticFocus(true)
                .create()
    }

    private fun openCameraStream(lensEngine: LensEngine) {
        try {
            binding?.preview?.start(lensEngine)
        } catch (exception: IOException) {
            Log.e(TAG, "Exception is $exception")
        }
    }

    private fun closeCameraStream() = binding?.preview?.stop()

    private fun releaseDetectionResources() {
        if (analyzer != null) {
            try {
                analyzer!!.stop()
            } catch (e: IOException) {
                Log.e(TAG, "Exception is $e")
            }
        }
    }

}