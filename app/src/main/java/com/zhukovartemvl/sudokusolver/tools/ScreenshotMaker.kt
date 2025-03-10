package com.zhukovartemvl.sudokusolver.tools

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class ScreenshotMaker(private val mediaProjectionKeeper: MediaProjectionKeeper) {

    suspend fun makeScreenShot(
        context: Context,
        onResult: (Mat) -> Unit
    ) = withContext(Dispatchers.Main) {
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val mediaProjectionIntent = mediaProjectionKeeper.getMediaProjectionIntent()

        val mediaProjection: MediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionIntent)

        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3)
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

        delay(50)

        imageReader.setOnImageAvailableListener({ reader: ImageReader ->
            reader.acquireLatestImage()?.use { image ->
                val formattedImage = formatImage(image = image)
                imageReader.close()
                virtualDisplay.release()
                mediaProjection.stop()
                onResult(formattedImage)
            }
        }, null)
    }

    private fun formatImage(image: Image): Mat {
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

        return grayscaleMat
    }
}
