package com.zhukovartemvl.sudokusolver.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FloatingTargetsView(
    scanReady: Boolean = false,
    onSolveClick: () -> Unit,
    onFeelingLuckyClick: () -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onScaleChange: (scale: Float) -> Unit
) {
    val state = rememberTransformableState { scaleChange, offset, _ ->
        onScaleChange(scaleChange)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Green.copy(alpha = 0.3f))
            .pointerInput(key1 = Unit) {
                detectDragGestures(onDrag = onDrag)
            }
            .transformable(state = state),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(times = 9) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 8.dp)
                            .background(color = Color.Red, shape = CircleShape)
                    )
                }
            }
        }
        if (scanReady) {
            Button(
                modifier = Modifier.align(alignment = Alignment.Center),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                onClick = onSolveClick
            ) {
                Text(text = "Solve", color = Color.White)
            }
        } else {
            Button(
                modifier = Modifier.align(alignment = Alignment.Center),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue),
                onClick = onFeelingLuckyClick
            ) {
                Text(text = "Feeling lucky", color = Color.White)
            }
        }
    }
}
