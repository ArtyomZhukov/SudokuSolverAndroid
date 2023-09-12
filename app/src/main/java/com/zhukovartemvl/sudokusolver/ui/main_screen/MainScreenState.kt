package com.zhukovartemvl.sudokusolver.ui.main_screen

data class MainScreenState(
    val isOverlayPermissionGranted: Boolean = false,
    val isAccessibilityPermissionGranted: Boolean = false,
    val isSudokuServiceEnabled: Boolean = false,
)
