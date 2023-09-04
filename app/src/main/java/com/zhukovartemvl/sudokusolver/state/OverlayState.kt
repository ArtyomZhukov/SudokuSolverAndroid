package com.zhukovartemvl.sudokusolver.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset

class OverlayState {
    var viewOffset by mutableStateOf(IntOffset.Zero)
}
