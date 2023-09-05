package com.zhukovartemvl.sudokusolver.model

import androidx.annotation.IntRange

sealed class Cell(open val index: Int) {
    data class Empty(override val index: Int) : Cell(index = index)
    data class Number(
        override val index: Int,
        @IntRange(from = 1, to = 9) val number: Int,
        val isStartNumber: Boolean = false
    ) : Cell(index = index)

    data class Note(
        override val index: Int,
        val possibleNumbers: Set<Int>
    ) : Cell(index = index)

    ;

    val rowIndex: Int
        get() = index / 9

    val columnIndex: Int
        get() = index % 9

    // Chunks
    val startChunkRowIndex: Int
        get() = rowIndex - (rowIndex % 3)

    val endChunkRowIndex: Int
        get() = startChunkRowIndex + 2

    val startChunkColumnIndex: Int
        get() = columnIndex - (columnIndex % 3)

    val endChunkColumnIndex: Int
        get() = startChunkColumnIndex + 2

    // Complex functions
    fun getNumbersInRow(gameField: Map<Int, Cell>): Set<Int> {
        val rowStartIndex = rowIndex * 9
        val rowEndIndex = rowStartIndex + 8

        return buildSet {
            (rowStartIndex..rowEndIndex).forEach { index ->
                (gameField[index] as? Number)?.let { cell ->
                    add(cell.number)
                }
            }
        }
    }

    fun getNumbersInColumn(gameField: Map<Int, Cell>): Set<Int> {
        return buildSet {
            repeat(times = 9) { row ->
                (gameField[row * 9 + columnIndex] as? Number)?.let { cell ->
                    add(cell.number)
                }
            }
        }
    }

    fun getNumbersInChunk(gameField: Map<Int, Cell>): Set<Int> {
        return buildSet {
            (startChunkRowIndex..endChunkRowIndex).forEach { row ->
                (startChunkColumnIndex..endChunkColumnIndex).forEach { column ->
                    val cellIndex = getCellIndex(rowIndex = row, columnIndex = column)
                    (gameField[cellIndex] as? Number)?.let { cell ->
                        add(cell.number)
                    }
                }
            }
        }
    }

    fun isValidNewNumberInCell(gameField: Map<Int, Cell>, newNumber: Int): Boolean {
        val numbers = getNumbersInRow(gameField) + getNumbersInColumn(gameField) + getNumbersInChunk(gameField)
        return !numbers.contains(newNumber)
    }

    private fun getCellIndex(rowIndex: Int, columnIndex: Int): Int {
        return rowIndex * 9 + columnIndex
    }
}
