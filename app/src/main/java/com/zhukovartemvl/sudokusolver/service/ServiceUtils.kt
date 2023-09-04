package com.zhukovartemvl.sudokusolver.service

import android.app.Service
import com.zhukovartemvl.sudokusolver.component.SudokuSolverOverlayComponent

fun serviceExit(overlayComponent: SudokuSolverOverlayComponent) {
    overlayComponent.removeOverlays()
    (overlayComponent.context as Service).stopSelf()
}
