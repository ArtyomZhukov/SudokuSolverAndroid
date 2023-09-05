package com.zhukovartemvl.sudokusolver.interactor

import android.content.Context
import androidx.annotation.IntRange
import com.zhukovartemvl.sudokusolver.model.Cell
import com.zhukovartemvl.sudokusolver.model.ClickTarget
import com.zhukovartemvl.sudokusolver.model.TargetsParams
import com.zhukovartemvl.sudokusolver.util.ImageCVScanner
import kotlinx.coroutines.delay
import org.opencv.core.Mat

class SudokuSolverInteractor {

    private var gameFieldTargets = listOf<ClickTarget>()
    private var numbersTargets = listOf<ClickTarget>()

    private var gameFieldParams: TargetsParams = TargetsParams(width = 0, height = 0, xPosition = 0, yPosition = 0)

    private var sudokuNumbers = listOf<Int>()

    var sudokuCells: List<Cell> = listOf()
        private set

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
        val gameField = mutableMapOf<Int, Cell>()

        sudokuNumbers.forEachIndexed { index, number ->
            gameField[index] = if (number == 0) {
                Cell.Empty(index = index)
            } else {
                Cell.Number(index = index, number = number, isStartNumber = true)
            }
        }

        var changed: Boolean
        var notChangedTimes = 0
        do {
            val gameFieldAtStart = gameField.toMap()

            gameField.filter { it.value is Cell.Empty }
                .map { (_, cell) ->
                    if (cell is Cell.Empty) {
                        val newCell = cell.fillCell(gameField = gameField)
                        gameField[newCell.index] = newCell
                    }
                }

            getCellsInChunks(gameField = gameField).forEach { cellsChunk ->
                val reformattedChunk = cellsChunk.reformatCellsChunk()
                reformattedChunk.forEach { cell ->
                    if (checkIsValidNumber(gameField = gameField, newCell = cell)) {
                        gameField[cell.index] = cell
                    }
                }
            }

            gameField.filter { it.value is Cell.Note }
                .map { (_, cell) ->
                    if (cell is Cell.Note) {
                        val newCell = cell.fillCell(gameField = gameField)
                        if (checkIsValidNumber(gameField = gameField, newCell = cell)) {
                            gameField[newCell.index] = newCell
                        }
                    }
                }

            gameField.filter { it.value !is Cell.Number }
                .map { (_, cell) ->
                    val newCell = cell.fillCell(gameField = gameField)
                    if (checkIsValidNumber(gameField = gameField, newCell = cell)) {
                        gameField[newCell.index] = newCell
                    }
                }


            changed = !isGameFieldsEquals(first = gameFieldAtStart, second = gameField.toMap())
            if (!changed) {
                notChangedTimes += 1
            }
        } while (notChangedTimes <= 3)

        println("---end")
        println(gameField.toString())

        return buildList {
            gameField.forEach { (index, cell) ->
                add(index, cell)
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
                    delay(100)
                    val target = numbersTargets[number - 1]
                    currentNumber = number
                    clickOnTarget(target.xPosition, target.yPosition)
                    delay(100)
                }
                cells.forEach { cell ->
                    val target = gameFieldTargets[cell.index]
                    clickOnTarget(target.xPosition, target.yPosition)
                    delay(30)
                }
            }
        sudokuCells = listOf()
        onComplete()
    }

    private fun isGameFieldsEquals(first: Map<Int, Cell>, second: Map<Int, Cell>): Boolean {
        first.forEach { (firstIndex, firstCell) ->
            val secondCell = second[firstIndex] ?: return false
            if (firstCell != secondCell) {
                return false
            }
        }
        return true
    }

    private fun checkIsValidNumber(gameField: Map<Int, Cell>, newCell: Cell): Boolean {
        val currentCell = gameField[newCell.index] ?: return false

        if (currentCell is Cell.Number) {
            return false
        }

        if (newCell !is Cell.Number) {
            return true
        }

        return newCell.isValidNewNumberInCell(gameField = gameField, newNumber = newCell.number)
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

    private fun Cell.Note.fillCell(gameField: Map<Int, Cell>): Cell {
        val possibleNumbers = possibleNumbers.toMutableSet()

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

    private fun List<Cell>.reformatCellsChunk(): List<Cell> {
        val numbers = this.filterIsInstance<Cell.Number>().toMutableList()

        val intNumbers = numbers.map { it.number }.toSet()

        val notes = this.filterIsInstance<Cell.Note>().map { note ->
            note.copy(possibleNumbers = note.possibleNumbers - intNumbers)
        }

        val filteredNotes = buildList {
            notes.forEach { note ->
                if (note.possibleNumbers.size == 1) {
                    numbers.add(Cell.Number(index = note.index, number = note.possibleNumbers.first()))
                } else {
                    add(note)
                }
            }
        }.toMutableList()

        if (filteredNotes.isEmpty()) {
            return numbers
        }

        findDuplicates(notes = filteredNotes).forEach { duplicateNote ->
            filteredNotes.forEachIndexed { index, note ->
                if (note.possibleNumbers != duplicateNote.possibleNumbers) {
                    val newPossibleNumbers = note.possibleNumbers - duplicateNote.possibleNumbers
                    filteredNotes[index] = note.copy(possibleNumbers = newPossibleNumbers)
                }
            }
        }

        return buildList {
            addAll(numbers)
            addAll(filteredNotes)
        }
    }

    private fun Cell.fillCell(gameField: Map<Int, Cell>): Cell {
        if (this is Cell.Number) {
            return this
        }
        val possibleNumbers = (1..9).toMutableSet()

        possibleNumbers.removeAll(getNumbersInColumn(gameField = gameField))
        if (possibleNumbers.size == 1) {
            return Cell.Number(index = index, number = possibleNumbers.first())
        }

        possibleNumbers.removeAll(getNumbersInRow(gameField = gameField))
        if (possibleNumbers.size == 1) {
            return Cell.Number(index = index, number = possibleNumbers.first())
        }

        possibleNumbers.removeAll(getNumbersInChunk(gameField = gameField))
        if (possibleNumbers.size == 1) {
            return Cell.Number(index = index, number = possibleNumbers.first())
        }

        return if (this is Cell.Note) {
            val numbers = if (this.possibleNumbers.size > possibleNumbers.size) possibleNumbers else this.possibleNumbers
            Cell.Note(index = index, possibleNumbers = numbers)
        } else {
            Cell.Note(index = index, possibleNumbers = possibleNumbers)
        }
    }

    private fun getCellsInChunks(gameField: Map<Int, Cell>): List<List<Cell>> {
        return buildList {
            for (row in 0 until 9 step 3) {
                for (column in 0 until 9 step 3) {
                    val chunk = mutableListOf<Cell>()

                    for (rowOffset in 0 until 3) {
                        for (colOffset in 0 until 3) {
                            val index = (row + rowOffset) * 9 + column + colOffset
                            gameField[index]?.let { cell ->
                                chunk.add(cell)
                            }
                        }
                    }
                    add(chunk)
                }
            }
        }
    }

    private fun findDuplicates(notes: List<Cell.Note>): List<Cell.Note> {
        return buildList {
            val numberCount = mutableMapOf<Set<Int>, Int>()
            notes.forEach { note ->
                val count = numberCount.getOrDefault(note.possibleNumbers, 0)
                if (count == 1) {
                    add(note)
                }
                numberCount[note.possibleNumbers] = count + 1
            }
        }
    }
}
