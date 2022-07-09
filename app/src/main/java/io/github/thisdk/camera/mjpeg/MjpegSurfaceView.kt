package io.github.thisdk.camera.mjpeg

import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.StyleableRes
import io.github.thisdk.camera.R


class MjpegSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback, MjpegView {

    private val mMjpegView: MjpegView

    private fun getPropertyBoolean(
        attributeSet: AttributeSet?,
        @StyleableRes attrs: IntArray?,
        attrIndex: Int
    ): Boolean {
        val typedArray = context.theme
            .obtainStyledAttributes(attributeSet, attrs!!, 0, 0)
        return try {
            typedArray.getBoolean(attrIndex, false)
        } finally {
            typedArray.recycle()
        }
    }

    private fun getPropertyColor(
        attributeSet: AttributeSet?,
        @StyleableRes attrs: IntArray?,
        attrIndex: Int
    ): Int {
        val typedArray = context.theme
            .obtainStyledAttributes(attributeSet, attrs!!, 0, 0)
        return try {
            typedArray.getColor(attrIndex, -1)
        } finally {
            typedArray.recycle()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        (mMjpegView as AbstractMjpegView).onSurfaceCreated(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        (mMjpegView as AbstractMjpegView).onSurfaceChanged(holder, format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        (mMjpegView as AbstractMjpegView).onSurfaceDestroyed(holder)
    }

    override fun setSource(stream: MjpegInputStream) {
        mMjpegView.setSource(stream)
    }

    override fun setDisplayMode(mode: DisplayMode) {
        mMjpegView.setDisplayMode(mode)
    }

    override fun showFps(show: Boolean) {
        mMjpegView.showFps(show)
    }

    override fun flipSource(flip: Boolean) {
        mMjpegView.flipSource(flip)
    }

    override fun flipHorizontal(flip: Boolean) {
        mMjpegView.flipHorizontal(flip)
    }

    override fun flipVertical(flip: Boolean) {
        mMjpegView.flipVertical(flip)
    }

    override fun setRotate(degrees: Float) {
        mMjpegView.setRotate(degrees)
    }

    override fun stopPlayback() {
        mMjpegView.stopPlayback()
    }

    override val isStreaming: Boolean
        get() = mMjpegView.isStreaming

    override fun setResolution(width: Int, height: Int) {
        mMjpegView.setResolution(width, height)
    }

    override fun freeCameraMemory() {
        mMjpegView.freeCameraMemory()
    }

    override fun setOnFrameCapturedListener(onFrameCapturedListener: OnFrameCapturedListener) {
        mMjpegView.setOnFrameCapturedListener(onFrameCapturedListener)
    }

    override fun setCustomBackgroundColor(backgroundColor: Int) {
        mMjpegView.setCustomBackgroundColor(backgroundColor)
    }

    override fun setFpsOverlayBackgroundColor(overlayBackgroundColor: Int) {
        mMjpegView.setFpsOverlayBackgroundColor(overlayBackgroundColor)
    }

    override fun setFpsOverlayTextColor(overlayTextColor: Int) {
        mMjpegView.setFpsOverlayTextColor(overlayTextColor)
    }

    override val surfaceView: SurfaceView
        get() = this

    override fun resetTransparentBackground() {
        mMjpegView.resetTransparentBackground()
    }

    override fun setTransparentBackground() {
        mMjpegView.setTransparentBackground()
    }

    override fun clearStream() {
        mMjpegView.clearStream()
    }

    override fun setPlayerStreamStateCallback(onPlayerStreamStateCallback: OnPlayerStreamStateCallback) {
        mMjpegView.setPlayerStreamStateCallback(onPlayerStreamStateCallback)
    }

    init {
        val transparentBackground = getPropertyBoolean(
            attrs,
            R.styleable.MjpegSurfaceView,
            R.styleable.MjpegSurfaceView_transparentBackground
        )
        val backgroundColor = getPropertyColor(
            attrs,
            R.styleable.MjpegSurfaceView,
            R.styleable.MjpegSurfaceView_backgroundColor
        )
        if (transparentBackground) {
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSPARENT)
        }
        mMjpegView = MjpegViewDefault(this, this, transparentBackground)
        if (backgroundColor != -1) {
            setCustomBackgroundColor(backgroundColor)
        }
    }
}