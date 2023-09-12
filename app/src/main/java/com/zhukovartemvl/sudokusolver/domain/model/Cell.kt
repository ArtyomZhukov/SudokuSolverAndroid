package com.zhukovartemvl.sudokusolver.domain.model

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

    private val rowIndex: Int
        get() = index / 9

    private val columnIndex: Int
        get() = index % 9

    // Chunks
    private val startChunkRowIndex: Int
        get() = rowIndex - (rowIndex % 3)

    private val endChunkRowIndex: Int
        get() = startChunkRowIndex + 2

    private val startChunkColumnIndex: Int
        get() = columnIndex - (columnIndex % 3)

    private val endChunkColumnIndex: Int
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

    private fun getCellIndex(rowIndex: Int, columnIndex: Int): Int {
        return rowIndex * 9 + columnIndex
    }
}
