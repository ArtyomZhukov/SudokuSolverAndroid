package com.zhukovartemvl.sudokusolver.service

import androidx.compose.ui.unit.IntOffset
import com.zhukovartemvl.sudokusolver.domain.model.ClickTarget
import com.zhukovartemvl.sudokusolver.domain.model.TargetsParams

data class SudokuSolverServiceState(
    val overlayState: ServiceOverlayState = ServiceOverlayState.Init,

    val initGameFieldSize: Int = 0,

    val gameFieldOverlayOffset: IntOffset = IntOffset.Zero,
    val numbersTargetOffset: IntOffset = IntOffset.Zero,
    val floatingButtonOffset: IntOffset = IntOffset.Zero,
    val happyHappyHappyCatOffset: IntOffset = IntOffset.Zero,

    var gameFieldTargets: List<ClickTarget> = listOf(),
    var numbersTargets: List<ClickTarget> = listOf(),
    var gameFieldParams: TargetsParams = TargetsParams.EMPTY,
    var sudokuNumbers: List<Int> = listOf()
)

sealed interface ServiceOverlayState {
    data object Init : ServiceOverlayState
    data object Default : ServiceOverlayState
    data object FloatingButton : ServiceOverlayState
    data object MakingScreenshot : ServiceOverlayState
    data object SudokuRecognizing : ServiceOverlayState
    data object SudokuSolving : ServiceOverlayState
    data object SudokuClicking : ServiceOverlayState
}
