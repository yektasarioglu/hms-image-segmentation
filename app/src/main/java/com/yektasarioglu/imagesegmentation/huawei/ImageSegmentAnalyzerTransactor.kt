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

import android.graphics.Bitmap
import android.graphics.Matrix

import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation

class ImageSegmentAnalyzerTransactor(private val mOverlay: GraphicOverlay) : MLTransactor<MLImageSegmentation> {

    private var isFront = true

    override fun transactResult(result: MLAnalyzer.Result<MLImageSegmentation>) {
        val imageSegmentationResult = result.analyseList
        var bitmap = imageSegmentationResult.valueAt(0).getForeground()
        if (isFront) {
            bitmap = flipImage(bitmap)
        }
        mOverlay.clear()
        val cameraImageGraphic = CameraImageGraphic(mOverlay, bitmap)
        mOverlay.addGraphic(cameraImageGraphic)
        mOverlay.postInvalidate()
    }

    override fun destroy() { }

    private fun flipImage(bitmap: Bitmap): Bitmap {
        val m = Matrix()
        m.setScale(-1f, 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    fun setFront(isFront: Boolean) {
        this.isFront = isFront
    }

}