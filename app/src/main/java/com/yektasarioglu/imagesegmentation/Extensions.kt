package com.yektasarioglu.imagesegmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri

val Any.TAG: String
    get() = javaClass.simpleName

fun Bitmap.addBackgroundColor(backgroundColor: Int) : Bitmap {
    val newBitmap = Bitmap.createBitmap(width, height, config)
    val canvas = Canvas(newBitmap)
    canvas.drawColor(backgroundColor)
    canvas.drawBitmap(this, 0f, 0f, null)
    return newBitmap
}

fun Uri.toDrawable(context: Context) : Drawable {
    val inputStream = context.contentResolver?.openInputStream(this)
    return Drawable.createFromStream(inputStream, this.toString())
}