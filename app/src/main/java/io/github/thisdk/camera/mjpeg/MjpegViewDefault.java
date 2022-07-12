package io.github.thisdk.camera.mjpeg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MjpegViewDefault extends AbstractMjpegView {
    private static final String TAG = MjpegViewDefault.class.getSimpleName();

    private final SurfaceHolder.Callback mSurfaceHolderCallback;
    private final SurfaceView mSurfaceView;
    private final boolean transparentBackground;

    private MjpegViewThread thread;
    private MjpegInputStreamDefault mIn = null;
    private boolean showFps = false;
    private boolean flipHorizontal = false;
    private boolean flipVertical = false;
    private float rotateDegrees = 0;
    private volatile boolean mRun = false;
    private volatile boolean surfaceDone = false;
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int backgroundColor;
    private int ovlPos;
    private int displayWidth;
    private int displayHeight;
    private int displayMode;
    private boolean resume = false;

    private OnPlayerStreamStateCallback playerStreamStateCallback;
    private long streamErrorMis = 0L;
    private int streamErrorVal = 0;

    private OnFrameCapturedListener onFrameCapturedListener;

    MjpegViewDefault(SurfaceView surfaceView, SurfaceHolder.Callback callback, boolean transparentBackground) {
        this.mSurfaceView = surfaceView;
        this.mSurfaceHolderCallback = callback;
        this.transparentBackground = transparentBackground;
        init();
    }

    Bitmap flip(Bitmap src) {
        Matrix m = new Matrix();
        float sx = flipHorizontal ? -1 : 1;
        float sy = flipVertical ? -1 : 1;
        m.preScale(sx, sy);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    Bitmap rotate(Bitmap src, float degrees) {
        Matrix m = new Matrix();
        m.setRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
    }

    private void init() {

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(mSurfaceHolderCallback);
        thread = new MjpegViewThread(holder);
        mSurfaceView.setFocusable(true);
        if (!resume) {
            resume = true;
            overlayPaint = new Paint();
            overlayPaint.setTextAlign(Paint.Align.LEFT);
            overlayPaint.setTextSize(12);
            overlayPaint.setTypeface(Typeface.DEFAULT);
            overlayTextColor = Color.WHITE;
            overlayBackgroundColor = Color.BLACK;
            backgroundColor = Color.BLACK;
            ovlPos = MjpegViewDefault.POSITION_LOWER_RIGHT;
            displayMode = MjpegViewDefault.SIZE_STANDARD;
            displayWidth = mSurfaceView.getWidth();
            displayHeight = mSurfaceView.getHeight();
        }
    }

    void _startPlayback() {
        if (mIn != null && thread != null) {
            mRun = true;
            mSurfaceView.destroyDrawingCache();
            thread.start();
        }
    }

    void _resumePlayback() {
        mRun = true;
        init();
        thread.start();
    }

    synchronized void _stopPlayback() {
        mRun = false;
        boolean retry = true;
        while (retry) {
            try {
                if (thread != null) {
                    thread.join(500);
                }
                retry = false;
            } catch (InterruptedException e) {
                Log.e(TAG, "error stopping playback thread", e);
            }
        }

        if (mIn != null) {
            try {
                mIn.close();
            } catch (IOException e) {
                Log.e(TAG, "error closing input stream", e);
            }
            mIn = null;
        }
    }

    void _surfaceChanged(int w, int h) {
        if (thread != null) {
            thread.setSurfaceSize(w, h);
        }
    }

    void _surfaceDestroyed() {
        surfaceDone = false;
        _stopPlayback();
        if (thread != null) {
            thread = null;
        }
    }

    void _frameCapturedWithByteData(byte[] imageByte, byte[] header) {
        if (onFrameCapturedListener != null) {
            onFrameCapturedListener.onFrameCapturedWithHeader(imageByte, header);
        }
    }

    void _frameCapturedWithBitmap(Bitmap bitmap) {
        if (onFrameCapturedListener != null) {
            onFrameCapturedListener.onFrameCaptured(bitmap);
        }
    }

    void _surfaceCreated() {
        surfaceDone = true;
    }

    void _showFps(boolean b) {
        showFps = b;
    }

    void _flipHorizontal(boolean b) {
        flipHorizontal = b;
    }

    void _flipVertical(boolean b) {
        flipVertical = b;
    }


    void _setSource(MjpegInputStreamDefault source) {
        mIn = source;
        if (!resume) {
            _startPlayback();
        } else {
            _resumePlayback();
        }
    }

    void setDisplayMode(int s) {
        displayMode = s;
    }

    @Override
    public void onSurfaceCreated(@NonNull SurfaceHolder holder) {
        _surfaceCreated();
    }

    @Override
    public void onSurfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        _surfaceChanged(width, height);
    }

    @Override
    public void onSurfaceDestroyed(@NonNull SurfaceHolder holder) {
        _surfaceDestroyed();
    }

    @Override
    public void setSource(@NonNull MjpegInputStream stream) {
        if (!(stream instanceof MjpegInputStreamDefault)) {
            throw new IllegalArgumentException("stream must be an instance of MjpegInputStreamDefault");
        }
        _setSource((MjpegInputStreamDefault) stream);
    }

    @Override
    public void setDisplayMode(DisplayMode mode) {
        setDisplayMode(mode.getValue());
    }

    @Override
    public void showFps(boolean show) {
        _showFps(show);
    }

    @Override
    public void flipSource(boolean flip) {
        _flipHorizontal(flip);
    }

    @Override
    public void flipHorizontal(boolean flip) {
        _flipHorizontal(flip);
    }

    @Override
    public void flipVertical(boolean flip) {
        _flipVertical(flip);
    }

    @Override
    public void setRotate(float degrees) {
        rotateDegrees = degrees;
    }

    @Override
    public void stopPlayback() {
        _stopPlayback();
    }

    @Override
    public boolean isStreaming() {
        return mRun;
    }

    @Override
    public void setResolution(int width, int height) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void freeCameraMemory() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setOnFrameCapturedListener(@NonNull OnFrameCapturedListener onFrameCapturedListener) {
        this.onFrameCapturedListener = onFrameCapturedListener;
    }

    @Override
    public void setCustomBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void setFpsOverlayBackgroundColor(int overlayBackgroundColor) {
        this.overlayBackgroundColor = overlayBackgroundColor;
    }

    @Override
    public void setFpsOverlayTextColor(int overlayTextColor) {
        this.overlayTextColor = overlayTextColor;
    }

    @NonNull
    @Override
    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    @Override
    public void resetTransparentBackground() {
        mSurfaceView.setZOrderOnTop(false);
        mSurfaceView.getHolder().setFormat(PixelFormat.OPAQUE);
    }

    @Override
    public void setTransparentBackground() {
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void clearStream() {
        Canvas c = null;

        try {
            c = mSurfaceView.getHolder().lockCanvas();
            c.drawColor(0, PorterDuff.Mode.CLEAR);
        } finally {
            if (c != null) {
                mSurfaceView.getHolder().unlockCanvasAndPost(c);
            } else {
                Log.w(TAG, "couldn't unlock surface canvas");
            }
        }
    }

    @Override
    public void setPlayerStreamStateCallback(@NonNull OnPlayerStreamStateCallback onPlayerStreamStateCallback) {
        this.playerStreamStateCallback = onPlayerStreamStateCallback;
    }

    class MjpegViewThread extends Thread {

        private final SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private Bitmap ovl;

        MjpegViewThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
        }

        private Rect destRect(int bmw, int bmh) {

            int temp_x;
            int temp_y;

            if (displayMode == MjpegViewDefault.SIZE_STANDARD) {
                temp_x = (displayWidth / 2) - (bmw / 2);
                temp_y = (displayHeight / 2) - (bmh / 2);
                return new Rect(temp_x, temp_y, bmw + temp_x, bmh + temp_y);
            }
            if (displayMode == MjpegViewDefault.SIZE_BEST_FIT) {
                float bitmap_sp = (float) bmw / (float) bmh;
                bmw = displayWidth;
                bmh = (int) (displayWidth / bitmap_sp);
                if (bmh > displayHeight) {
                    bmh = displayHeight;
                    bmw = (int) (displayHeight * bitmap_sp);
                }
                temp_x = (displayWidth / 2) - (bmw / 2);
                temp_y = (displayHeight / 2) - (bmh / 2);
                return new Rect(temp_x, temp_y, bmw + temp_x, bmh + temp_y);
            }
            if (displayMode == MjpegViewDefault.SIZE_SCALE_FIT) {
                float bm_asp = ((float) bmw / (float) bmh);
                temp_x = 0;
                temp_y = 0;
                if (bmw < displayWidth) {
                    bmw = displayWidth;
                    bmh = (int) (displayWidth / bm_asp);
                    temp_y = (displayHeight - bmh) / 4;
                }
                return new Rect(temp_x, temp_y, bmw, bmh + temp_y);
            }
            if (displayMode == MjpegViewDefault.SIZE_FULLSCREEN)
                return new Rect(0, 0, displayWidth, displayHeight);
            return null;
        }

        void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                displayWidth = width;
                displayHeight = height;
            }
        }

        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int b_width = b.width() + 2;
            int b_height = b.height() + 2;
            Bitmap bm = Bitmap.createBitmap(b_width, b_height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, b_width, b_height, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left + 1, (b_height / 2f) - ((p.ascent() + p.descent()) / 2) + 1, p);
            return bm;
        }

        public void run() {
            long start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bitmap;
            int width;
            int height;
            Rect destRect;
            Canvas canvas = null;
            Paint paint = new Paint();
            String fps;
            while (mRun) {
                if (surfaceDone) {
                    try {
                        canvas = mSurfaceHolder.lockCanvas();
                        if (canvas == null) {
                            Log.w(TAG, "null canvas, skipping render");
                            continue;
                        }
                        synchronized (mSurfaceHolder) {
                            try {
                                byte[] header = mIn.readHeader();
                                byte[] imageData = mIn.readMjpegFrame(header);
                                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(imageData));
                                if (flipHorizontal || flipVertical)
                                    bitmap = flip(bitmap);
                                if (rotateDegrees != 0)
                                    bitmap = rotate(bitmap, rotateDegrees);
                                _frameCapturedWithByteData(imageData, header);
                                _frameCapturedWithBitmap(bitmap);
                                destRect = destRect(bitmap.getWidth(), bitmap.getHeight());
                                if (transparentBackground) {
                                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                } else {
                                    canvas.drawColor(backgroundColor);
                                }
                                canvas.drawBitmap(bitmap, null, destRect, paint);
                                if (showFps && destRect != null) {
                                    paint.setXfermode(mode);
                                    if (ovl != null) {
                                        height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                        width = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();
                                        canvas.drawBitmap(ovl, width, height, null);
                                    }
                                    paint.setXfermode(null);
                                    frameCounter++;
                                    if ((System.currentTimeMillis() - start) >= 1000) {
                                        fps = frameCounter + "fps";
                                        frameCounter = 0;
                                        start = System.currentTimeMillis();
                                        ovl = makeFpsOverlay(overlayPaint, fps);
                                    }
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "encountered exception during render", e);
                                if (System.currentTimeMillis() - streamErrorMis < 1000) {
                                    streamErrorVal++;
                                    if (streamErrorVal > 20) {
                                        mRun = false;
                                        if (playerStreamStateCallback != null) {
                                            playerStreamStateCallback.streamError();
                                        }
                                        streamErrorVal = 0;
                                    }
                                } else {
                                    streamErrorMis = System.currentTimeMillis();
                                    streamErrorVal = 0;
                                }
                            }
                        }
                    } finally {
                        if (canvas != null) {
                            mSurfaceHolder.unlockCanvasAndPost(canvas);
                        } else {
                            Log.w(TAG, "couldn't unlock surface canvas");
                        }
                    }
                }
            }
        }
    }
}
