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
import android.graphics.Canvas
import android.hardware.Camera
import android.util.AttributeSet
import android.view.View

import java.util.*

class GraphicOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val lock = Any()
    private val graphics: MutableList<BaseGraphic> = ArrayList()

    private var previewWidth = 0
    private var previewHeight = 0

    var widthScaleValue = 1.0f
        private set
    var heightScaleValue = 1.0f
        private set
    var cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
        private set

    fun clear() {
        synchronized(lock) { graphics.clear() }
        this.postInvalidate()
    }

    fun addGraphic(graphic: BaseGraphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun removeGraphic(graphic: BaseGraphic) {
        synchronized(lock) { graphics.remove(graphic) }
        this.postInvalidate()
    }

    fun setCameraInfo(width: Int, height: Int, facing: Int) {
        synchronized(lock) {
            previewWidth = width
            previewHeight = height
            cameraFacing = facing
        }
        this.postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                widthScaleValue = canvas.width.toFloat() / previewWidth.toFloat()
                heightScaleValue = canvas.height.toFloat() / previewHeight.toFloat()
            }
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }

}