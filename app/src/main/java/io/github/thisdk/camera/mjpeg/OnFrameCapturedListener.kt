package io.github.thisdk.camera.mjpeg

import android.graphics.Bitmap

interface OnFrameCapturedListener {
    fun onFrameCaptured(bitmap: Bitmap)
    fun onFrameCapturedWithHeader(bitmap: ByteArray, header: ByteArray)
}
