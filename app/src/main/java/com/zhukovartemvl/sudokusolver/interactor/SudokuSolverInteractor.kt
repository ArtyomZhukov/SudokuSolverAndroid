package com.zhukovartemvl.sudokusolver.interactor

import android.content.Context
import com.zhukovartemvl.sudokusolver.model.Cell
import com.zhukovartemvl.sudokusolver.model.ClickTarget
import com.zhukovartemvl.sudokusolver.model.TargetsParams
import com.zhukovartemvl.sudokusolver.util.ImageCVScanner
import com.zhukovartemvl.sudokusolver.util.SudokuSolver
import kotlinx.coroutines.delay
import org.opencv.core.Mat

class SudokuSolverInteractor {

    private var gameFieldTargets = listOf<ClickTarget>()
    private var numbersTargets = listOf<ClickTarget>()

    private var gameFieldParams: TargetsParams = TargetsParams(width = 0, height = 0, xPosition = 0, yPosition = 0)

    private var sudokuNumbers = listOf<Int>()

    var sudokuCells: List<Cell> = listOf()
        private set

    fun clear() {
        sudokuNumbers = listOf()
        sudokuCells = listOf()
    }

    fun setTargets(gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) {
        this.gameFieldParams = gameFieldParams

        val chunkSize = gameFieldParams.width / 9
        val xPositions = buildList {
            repeat(times = 9) { index ->
                add(gameFieldParams.xPosition + (chunkSize * index) + (chunkSize / 2))
            }
        }
        val yPositions = buildList {
            repeat(times = 9) { index ->
                add(gameFieldParams.yPosition + (chunkSize * index) + (chunkSize / 2) + statusBarHeight)
            }
        }
        gameFieldTargets = buildList {
            repeat(times = 9) { yIndex ->
                repeat(times = 9) { xIndex ->
                    val xPosition = xPositions[xIndex]
                    val yPosition = yPositions[yIndex]

                    val clickTarget = ClickTarget(
                        xPosition = xPosition,
                        yPosition = yPosition
                    )
                    val index = yIndex * 9 + xIndex
                    add(index, clickTarget)
                }
            }
        }
        numbersTargets = buildList {
            val yPosition = numbersTargetsParams.yPosition + (numbersTargetsParams.height / 2) + statusBarHeight

            val chunkWidth = numbersTargetsParams.width / 9

            repeat(times = 9) { index ->
                val xPosition = numbersTargetsParams.xPosition + (chunkWidth * index) + (chunkWidth / 2)
                add(ClickTarget(xPosition = xPosition, yPosition = yPosition))
            }
        }
    }

    fun scanScreenshot(context: Context, mat: Mat) {
        sudokuNumbers = ImageCVScanner.scanMat(context = context, gameFieldMat = mat, gameFieldParams = gameFieldParams)
        mat.release()
        sudokuCells = buildList {
            sudokuNumbers.forEachIndexed { index, number ->
                val cell = if (number == 0) {
                    Cell.Empty(index = index)
                } else {
                    Cell.Number(index = index, number = number, isStartNumber = true)
                }
                add(index = cell.index, element = cell)
            }
        }
    }

    fun solveSudoku(): List<Cell> {
        val result = SudokuSolver.solve(sudoku = sudokuNumbers)
        return result.mapIndexed { index, number ->
            if (number == 0) {
                Cell.Empty(index)
            } else {
                Cell.Number(index, number, isStartNumber = sudokuNumbers[index] != 0)
            }
        }
    }

    suspend fun startAutoClicker(sudoku: List<Cell>, clickOnTarget: (Int, Int) -> Unit, onComplete: suspend () -> Unit) {
        var currentNumber = 0
        sudoku.filterIsInstance<Cell.Number>()
            .filter { !it.isStartNumber }
            .groupBy { it.number }
            .forEach { (number, cells) ->
                if (number != currentNumber) {
                    delay(2)
                    val target = numbersTargets[number - 1]
                    currentNumber = number
                    clickOnTarget(target.xPosition, target.yPosition)
                    delay(2)
                }
                cells.forEach { cell ->
                    val target = gameFieldTargets[cell.index]
                    clickOnTarget(target.xPosition, target.yPosition)
                    delay(2)
                }
            }
        sudokuCells = listOf()
        onComplete()
    }
}
