package com.zhukovartemvl.sudokusolver.model

data class TargetsParams(
    val width: Int,
    val height: Int,
    val xPosition: Int,
    val yPosition: Int
) {
    companion object {
        val EMPTY = TargetsParams(width = 0, height = 0, xPosition = 0, yPosition = 0)
    }
}
