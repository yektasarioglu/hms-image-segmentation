package com.yektasarioglu.imagesegmentation.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment

import com.esafirm.imagepicker.features.ImagePicker

import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer

import com.yektasarioglu.imagesegmentation.TAG
import com.yektasarioglu.imagesegmentation.addBackgroundColor
import com.yektasarioglu.imagesegmentation.databinding.FragmentStaticBinding
import com.yektasarioglu.imagesegmentation.toDrawable

import java.io.IOException

class StaticHumanBodySegmentationFragment : Fragment() {

    private var binding: FragmentStaticBinding? = null

    private var analyzer: MLImageSegmentationAnalyzer? = null

    private var selectedImageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        binding = FragmentStaticBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAnalyzer()

        binding?.selectImageButton?.setOnClickListener { askImageFromUser() }
        binding?.analyzeButton?.setOnClickListener {
            analyze(getMLFrame(selectedImageBitmap ?: return@setOnClickListener))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseDetectionResources()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            binding?.apply {
                val drawable = image.uri.toDrawable(this@StaticHumanBodySegmentationFragment.activity as Context)

                binding?.comparisonSlider?.apply {
                    setAfterImage(null)
                    setBeforeImage(drawable)
                }

                selectedImageBitmap = drawable.toBitmap()

                analyzeButton.visibility = View.VISIBLE
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun askImageFromUser() = ImagePicker.create(this).single().start()

    private fun configureAfterImage(resultBitmap: Bitmap) {
        binding?.comparisonSlider?.apply {
            setAfterImage(BitmapDrawable(resources, resultBitmap.addBackgroundColor(Color.WHITE)))
        }
    }

    private fun initializeAnalyzer() {
        // The default mode is human body segmentation in fine mode. All segmentation results of human body segmentation are returned (pixel-level label information, human body image with a transparent background, gray-scale image with a white human body and black background, and an original image for segmentation).
        analyzer = MLAnalyzerFactory.getInstance().imageSegmentationAnalyzer
    }

    private fun getMLFrame(bitmap: Bitmap) : MLFrame {
        // Create an MLFrame object using the bitmap, which is the image data in bitmap format.
        return MLFrame.fromBitmap(bitmap)
    }

    private fun analyze(frame: MLFrame) {
        // Create a task to process the result returned by the image segmentation analyzer.
        val task: Task<MLImageSegmentation> = analyzer?.asyncAnalyseFrame(frame)!!

        // Asynchronously process the result returned by the image segmentation analyzer.
        task.addOnSuccessListener {
            // Detection success.
            Log.i(TAG, "Success - Result is  $it")
            configureAfterImage(it.foreground)
        }.addOnFailureListener {
            // Detection failure.
            Log.e(TAG, "Failure - Exception is $it")
        }
    }

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