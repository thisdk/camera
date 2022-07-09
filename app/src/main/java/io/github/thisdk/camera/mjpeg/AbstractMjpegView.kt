package io.github.thisdk.camera.mjpeg

import android.view.SurfaceHolder

abstract class AbstractMjpegView : MjpegView {
    abstract fun onSurfaceCreated(holder: SurfaceHolder)
    abstract fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int)
    abstract fun onSurfaceDestroyed(holder: SurfaceHolder)

    companion object {
        protected const val POSITION_LOWER_RIGHT = 6
        protected const val SIZE_STANDARD = 1
        protected const val SIZE_BEST_FIT = 4
        protected const val SIZE_SCALE_FIT = 16
        protected const val SIZE_FULLSCREEN = 8
    }
}
