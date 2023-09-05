package com.zhukovartemvl.sudokusolver.preferences

import android.content.Context

class SudokuPreferences(context: Context) {

    private val preferences = context.getSharedPreferences("sudokuPrefs", Context.MODE_PRIVATE)

    var gameFieldPosition: Pair<Int, Int>
        get() {
            val x = preferences.getInt(GAME_FIELD_X_POSITION, 0)
            val y = preferences.getInt(GAME_FIELD_Y_POSITION, 0)
            return x to y
        }
        set(value) {
            preferences.edit().putInt(GAME_FIELD_X_POSITION, value.first).apply()
            preferences.edit().putInt(GAME_FIELD_Y_POSITION, value.second).apply()
        }

    var gameFieldSize: Int
        get() = preferences.getInt(GAME_FIELD_SIZE, 0)
        set(value) {
            preferences.edit().putInt(GAME_FIELD_SIZE, value).apply()
        }

    var numbersTargetsYPosition: Int
        get() = preferences.getInt(NUMBERS_TARGETS_Y_POSITION, 0)
        set(value) {
            preferences.edit().putInt(NUMBERS_TARGETS_Y_POSITION, value).apply()
        }

    companion object {
        private const val GAME_FIELD_X_POSITION = "GAME_FIELD_X_POSITION"
        private const val GAME_FIELD_Y_POSITION = "GAME_FIELD_Y_POSITION"
        private const val GAME_FIELD_SIZE = "GAME_FIELD_SIZE"

        private const val NUMBERS_TARGETS_Y_POSITION = "NUMBERS_TARGETS_Y_POSITION"
    }
}
