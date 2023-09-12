package com.zhukovartemvl.sudokusolver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class SudokuSolverAutoClickerService : AccessibilityService() {

    companion object {
        private val _serviceStarted = mutableStateOf(false)
        val serviceStarted: State<Boolean> = _serviceStarted

        var instance: SudokuSolverAutoClickerService? = null
            private set(value) {
                field = value
                _serviceStarted.value = value != null
            }
    }

    fun makeClickOnPosition(xPos: Int, yPos: Int) {
        val path = Path()
        path.moveTo(xPos.toFloat(), yPos.toFloat())
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(path, 0, 1)
        )
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onServiceConnected() {
        instance = this
        super.onServiceConnected()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit
}
