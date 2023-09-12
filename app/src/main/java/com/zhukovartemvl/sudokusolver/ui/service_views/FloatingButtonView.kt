package com.zhukovartemvl.sudokusolver.ui.service_views

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun FloatingButtonView(
    onShowClick: () -> Unit,
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 60.dp)
            .background(color = Color.Cyan.copy(alpha = 0.5f), shape = CircleShape)
            .pointerInput(key1 = Unit) {
                detectDragGestures(onDrag = onDrag)
            }
    ) {
        IconButton(modifier = Modifier.fillMaxSize(), onClick = onShowClick) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
        }
    }
}
