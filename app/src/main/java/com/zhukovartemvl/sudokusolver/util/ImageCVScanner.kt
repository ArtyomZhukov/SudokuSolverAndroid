package com.zhukovartemvl.sudokusolver.util

import android.content.Context
import com.zhukovartemvl.sudokusolver.model.Match
import com.zhukovartemvl.sudokusolver.model.Region
import com.zhukovartemvl.sudokusolver.model.SudokuNumber
import com.zhukovartemvl.sudokusolver.model.TargetsParams
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import kotlin.math.roundToInt

object ImageCVScanner {

    fun scanMat(context: Context, gameFieldMat: Mat, gameFieldParams: TargetsParams): List<Int> {
        val numbersMats = loadNumbersMats(context)

        val gameFieldRegion = Region(
            x = gameFieldParams.xPosition,
            y = gameFieldParams.yPosition,
            width = gameFieldParams.width,
            height = gameFieldParams.height
        )
        val croppedGameFieldMat = gameFieldMat.crop(region = gameFieldRegion)

        val minSimilarity = 0.85

        val gameFieldMap = mutableMapOf<Int, Int>()

        numbersMats.forEachIndexed { index, numberMat ->
            croppedGameFieldMat.findMatches(template = numberMat, similarity = minSimilarity).forEach { match ->
                val position = findPositionAtGameField(gameFieldParams = gameFieldParams, match = match)
                if (position != -1) {
                    gameFieldMap[position] = index + 1
                }
            }
        }

        gameFieldMat.release()
        numbersMats.forEach { it.release() }

        return buildList {
            repeat(times = 81) { index ->
                add(gameFieldMap[index] ?: 0)
            }
        }
    }

    private fun findPositionAtGameField(gameFieldParams: TargetsParams, match: Match): Int {
        val rectWidth = gameFieldParams.width
        val rectHeight = gameFieldParams.height

        val point = match.region.center

        // Проверяем, что координаты точки находятся в пределах поля
        if (point.x < 0 || point.x > rectWidth || point.y < 0 || point.y > rectHeight) {
            return -1
        }

        // Вычисляем номер строки и столбца
        val row = (point.y * 9) / rectHeight + 1
        val column = (point.x * 9) / rectWidth + 1

        // Вычисляем номер ячейки
        return (row - 1) * 9 + column - 1
    }

    private fun loadNumbersMats(context: Context): List<Mat> {

        return buildList {
            val assets = context.assets
            SudokuNumber.values().forEach { number ->
                assets.open("numbers/${number.fileName}").use { inputStream ->
                    add(makeMat(stream = inputStream))
                }
            }
        }
    }

    private fun makeMat(stream: InputStream): Mat {
        val byteArray = stream.readBytes()

        return MatOfByte(*byteArray).use { matOfByte ->
            Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_GRAYSCALE)
        }
    }

    private fun Mat.crop(region: Region): Mat {
        val clippedRegion = Region(0, 0, width(), height())
            .clip(region)

        val rect = Rect(clippedRegion.x, clippedRegion.y, clippedRegion.width, clippedRegion.height)

        return Mat(this, rect)
    }

    private fun Mat.findMatches(template: Mat, similarity: Double) = sequence {
        val result = match(template)

        result.use {
            while (true) {
                val minMaxLocResult = Core.minMaxLoc(it)
                val score = minMaxLocResult.maxVal

                if (score >= similarity) {
                    val loc = minMaxLocResult.maxLoc
                    val region = Region(
                        loc.x.roundToInt(),
                        loc.y.roundToInt(),
                        template.width(),
                        template.height()
                    )

                    val match = Match(region, score)

                    yield(match)

                    // Flood fill eliminates the problem of nearby points to a high similarity point also having high similarity
                    result.floodFill(loc, 0.3, 0.0)
                } else {
                    break
                }
            }
        }
    }

    private fun Mat.match(template: Mat): Mat {
        val result = Mat()
        if (template.width() <= width() && template.height() <= height()) {
            Imgproc.matchTemplate(
                this,
                template,
                result,
                Imgproc.TM_CCOEFF_NORMED
            )
        }
        return result
    }

    private fun Mat.floodFill(startingPoint: Point, maxDiff: Double, newValue: Double) {
        Mat().use { mask ->
            Imgproc.floodFill(
                this, mask, startingPoint, Scalar(newValue),
                Rect(),
                Scalar(maxDiff), Scalar(maxDiff),
                Imgproc.FLOODFILL_FIXED_RANGE
            )
        }
    }
}
