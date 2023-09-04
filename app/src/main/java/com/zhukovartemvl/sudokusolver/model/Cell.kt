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


}
