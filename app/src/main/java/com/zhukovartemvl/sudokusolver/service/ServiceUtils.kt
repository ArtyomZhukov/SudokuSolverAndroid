package com.zhukovartemvl.sudokusolver.service

import android.app.Service
import com.zhukovartemvl.sudokusolver.service.component.SudokuSolverOverlayComponent

fun serviceExit(overlayComponent: SudokuSolverOverlayComponent) {
    overlayComponent.onDestroy()
    (overlayComponent.context as Service).stopSelf()
}
