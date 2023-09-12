package com.zhukovartemvl.sudokusolver.domain

import com.zhukovartemvl.sudokusolver.domain.model.Cell
import com.zhukovartemvl.sudokusolver.domain.model.ClickTarget
import com.zhukovartemvl.sudokusolver.domain.model.TargetsParams
import kotlinx.coroutines.delay

class SudokuSolverInteractor {

    fun createGameFieldTargets(gameFieldParams: TargetsParams, statusBarHeight: Int): List<ClickTarget> {
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
        return buildList {
            repeat(times = 9) { yIndex ->
                repeat(times = 9) { xIndex ->
                    val clickTarget = ClickTarget(xPosition = xPositions[xIndex], yPosition = yPositions[yIndex])
                    val index = yIndex * 9 + xIndex
                    add(index, clickTarget)
                }
            }
        }
    }

    fun createNumbersTargets(numbersTargetsParams: TargetsParams, statusBarHeight: Int): List<ClickTarget> {
        return buildList {
            val yPosition = numbersTargetsParams.yPosition + (numbersTargetsParams.height / 2) + statusBarHeight

            val chunkWidth = numbersTargetsParams.width / 9

            repeat(times = 9) { index ->
                val xPosition = numbersTargetsParams.xPosition + (chunkWidth * index) + (chunkWidth / 2)
                add(ClickTarget(xPosition = xPosition, yPosition = yPosition))
            }
        }
    }

    fun getCellsToClick(initialSudoku: List<Int>, solvedSudoku: List<Int>): List<Cell> {
        return buildList {
            solvedSudoku.forEachIndexed { index, number ->
                if (number != 0 && initialSudoku[index] == 0) {
                    add(Cell.Number(index, number))
                }
            }
        }
    }

    suspend fun startAutoClicker(
        sudoku: List<Cell>,
        gameFieldTargets: List<ClickTarget>,
        numbersTargets: List<ClickTarget>,
        clickOnTarget: (Int, Int) -> Unit,
        onComplete: suspend () -> Unit
    ) {
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
        onComplete()
    }
}
