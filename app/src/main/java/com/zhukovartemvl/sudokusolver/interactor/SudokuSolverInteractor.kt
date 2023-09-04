package com.zhukovartemvl.sudokusolver.interactor

import android.content.Context
import androidx.annotation.IntRange
import com.zhukovartemvl.sudokusolver.model.Cell
import com.zhukovartemvl.sudokusolver.model.ClickTarget
import com.zhukovartemvl.sudokusolver.model.TargetsParams
import com.zhukovartemvl.sudokusolver.util.ImageCVScanner
import org.opencv.core.Mat

class SudokuSolverInteractor {

    private var gameFieldTargets = listOf<ClickTarget>()
    private var numbersTargets = listOf<ClickTarget>()

    private var gameFieldParams: TargetsParams = TargetsParams(width = 0, height = 0, xPosition = 0, yPosition = 0)

    private var sudokuNumbers = listOf<Int>()

    fun setTargets(gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams) {
        this.gameFieldParams = gameFieldParams
        gameFieldTargets = buildList {
            val xPositions = buildList {
                val chunkWidth = gameFieldParams.width / 9

                repeat(times = 9) { index ->
                    add(gameFieldParams.xPosition + (chunkWidth * index) + (chunkWidth / 2))
                }
            }
            val yPositions = buildList {
                val chunkWidth = gameFieldParams.height / 9

                repeat(times = 9) { index ->
                    add(gameFieldParams.yPosition + (chunkWidth * index) + (chunkWidth / 2))
                }
            }
            repeat(times = 9) { yIndex ->
                repeat(times = 9) { xIndex ->
                    val clickTarget = ClickTarget(
                        xPosition = xPositions[xIndex],
                        yPosition = yPositions[yIndex]
                    )
                    add(clickTarget)
                }
            }
        }
        numbersTargets = buildList {
            val yPosition = numbersTargetsParams.yPosition + (numbersTargetsParams.height / 2)

            val chunkWidth = numbersTargetsParams.width / 9

            repeat(times = 9) { index ->
                val xPosition = numbersTargetsParams.xPosition + (chunkWidth * index) + (chunkWidth / 2)
                add(ClickTarget(xPosition = xPosition, yPosition = yPosition))
            }
        }
    }

    fun scanScreenshot(context: Context, mat: Mat): List<Int> {
        sudokuNumbers = ImageCVScanner.scanMat(context = context, gameFieldMat = mat, gameFieldParams = gameFieldParams)
        return sudokuNumbers
    }

    fun solveSudoku() {
        val gameField = mutableMapOf<Int, Cell>()

        sudokuNumbers.forEachIndexed { index, number ->
            gameField[index] = if (number == 0) {
                Cell.Empty(index = index)
            } else {
                Cell.Number(index = index, number = number, isStartNumber = true)
            }
        }

        // gameField.filter { it is Cell.Empty }

    }

    private fun Cell.Empty.fillCell(gameField: Map<Int, Cell>): Cell {
        val possibleNumbers = (1..9).toMutableSet()

        possibleNumbers.removeAll(getNumbersInRow(gameField = gameField))
        if (possibleNumbers.size == 1) {
            return Cell.Number(index = index, number = possibleNumbers.first())
        }

        possibleNumbers.removeAll(getNumbersInColumn(gameField = gameField))
        if (possibleNumbers.size == 1) {
            return Cell.Number(index = index, number = possibleNumbers.first())
        }

        possibleNumbers.removeAll(getNumbersInChunk(gameField = gameField))
        if (possibleNumbers.size == 1) {
            return Cell.Number(index = index, number = possibleNumbers.first())
        }

        return Cell.Note(index = index, possibleNumbers = possibleNumbers)
    }

    private fun Cell.getNumbersInRow(gameField: Map<Int, Cell>): Set<Int> {
        val rowStartIndex = rowIndex * 9
        val rowEndIndex = rowStartIndex + 8

        return buildSet {
            (rowStartIndex..rowEndIndex).forEach { index ->
                (gameField[index] as? Cell.Number)?.let { cell ->
                    add(cell.number)
                }
            }
        }
    }

    private fun Cell.getNumbersInColumn(gameField: Map<Int, Cell>): Set<Int> {
        return buildSet {
            repeat(times = 9) { row ->
                (gameField[row * 9 + columnIndex] as? Cell.Number)?.let { cell ->
                    add(cell.number)
                }
            }
        }
    }

    private fun Cell.getNumbersInChunk(gameField: Map<Int, Cell>): Set<Int> {
        return buildSet {
            (startChunkRowIndex..endChunkRowIndex).forEach { row ->
                (startChunkColumnIndex..endChunkColumnIndex).forEach { column ->
                    val cellIndex = getCellIndex(rowIndex = row, columnIndex = column)
                    (gameField[cellIndex] as? Cell.Number)?.let { cell ->
                        add(cell.number)
                    }
                }
            }
        }
    }

    private fun getCellIndex(rowIndex: Int, columnIndex: Int): Int {
        return rowIndex * 9 + columnIndex
    }
}
