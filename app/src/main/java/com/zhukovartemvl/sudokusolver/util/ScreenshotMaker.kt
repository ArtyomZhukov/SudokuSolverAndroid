package com.zhukovartemvl.sudokusolver.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import kotlinx.coroutines.delay
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object ScreenshotMaker {

    @SuppressLint("WrongConstant")
    suspend fun makeScreenShot(context: Context, mediaProjectionIntent: Intent, onResult: (Mat) -> Unit, onFailure: () -> Unit) {
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionIntent)

        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        val virtualDisplay = mediaProjection.createVirtualDisplay(
            "Screenshot",
            width,
            height,
            context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )

        delay(100)

        imageReader.acquireLatestImage()?.use { image ->
            val plane = image.planes[0]
            val buffer = plane.buffer

            val rowStride = plane.rowStride.toLong()
            val bufferMat = Mat()

            val heightPx = image.height
            val widthPx = image.width

            val tempMat = Mat(heightPx, widthPx, CvType.CV_8UC4, buffer, rowStride)
            tempMat.copyTo(bufferMat)

            val grayscaleMat = Mat()
            Imgproc.cvtColor(bufferMat, grayscaleMat, Imgproc.COLOR_RGBA2GRAY)

            bufferMat.release()
            tempMat.release()
            image.close()
            imageReader.close()

            onResult(grayscaleMat)
        } ?: onFailure()

        imageReader.close()
        virtualDisplay.release()
        mediaProjection.stop()
    }
}
