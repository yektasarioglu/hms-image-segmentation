/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yektasarioglu.imagesegmentation.huawei

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup

import com.huawei.hms.mlsdk.common.LensEngine

import java.io.IOException

class LensEnginePreview(private val mContext: Context, attrs: AttributeSet?) : ViewGroup(mContext, attrs) {

    private val mSurfaceView: SurfaceView = SurfaceView(mContext)

    private val isPortraitMode: Boolean
        get() {
            val orientation = mContext.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            Log.d(TAG, "isPortraitMode returning false by default")
            return false
        }

    private var mStartRequested = false
    private var mSurfaceAvailable = false
    private var mLensEngine: LensEngine? = null
    private var mOverlay: GraphicOverlay? = null

    init {
        mSurfaceView.holder.addCallback(SurfaceCallback())
        this.addView(mSurfaceView)
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?, overlay: GraphicOverlay?) {
        mOverlay = overlay
        start(lensEngine)
    }

    @Throws(IOException::class)
    fun start(lensEngine: LensEngine?) {
        if (lensEngine == null) {
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine != null) {
            mStartRequested = true
            startIfReady()
        }
    }

    fun stop() {
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
    }

    fun release() {
        if (mLensEngine != null) {
            mLensEngine!!.release()
            mLensEngine = null
        }
    }

    @Throws(IOException::class)
    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mLensEngine!!.run(mSurfaceView.holder)
            if (mOverlay != null) {
                val size = mLensEngine!!.displayDimension
                val min = Math.min(size.width, size.height)
                val max = Math.max(size.width, size.height)
                if (mContext.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay!!.setCameraInfo(min, max, mLensEngine!!.lensType)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mLensEngine!!.lensType)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var previewWidth = 480
        var previewHeight = 360
        if (mLensEngine != null) {
            val size = mLensEngine!!.displayDimension
            if (size != null) {
                previewWidth = size.width
                previewHeight = size.height
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = previewWidth
            previewWidth = previewHeight
            previewHeight = tmp
        }
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions. We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - viewHeight) / 2
        } else {
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / 2
        }
        for (i in 0 until this.childCount) {
            // One dimension will be cropped. We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(-1 * childXOffset, -1 * childYOffset, childWidth - childXOffset,
                    childHeight - childYOffset)
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    companion object {
        private const val TAG = "LensEnginePreview"
    }

}