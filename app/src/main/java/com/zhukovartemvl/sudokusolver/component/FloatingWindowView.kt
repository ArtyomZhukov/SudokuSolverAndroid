package com.zhukovartemvl.sudokusolver.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhukovartemvl.sudokusolver.model.Cell

@Composable
fun FloatingWindowView(
    sudoku: List<Cell> = listOf(),
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
    onScaleChange: (scale: Float) -> Unit,
    onScanClick: () -> Unit,
    onHideClick: () -> Unit,
    onCloseClick: () -> Unit,
    onCenterHorizontallyClick: () -> Unit,
    onMinusZoomClick: () -> Unit,
    onPlusZoomClick: () -> Unit
) {
    val state = rememberTransformableState { scaleChange, _, _ ->
        onScaleChange(scaleChange)
    }

    val buttonColor = Color.Blue.copy(alpha = 0.2f)
    val buttonTextColor = Color.White.copy(alpha = 0.3f)

    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray.copy(alpha = 0.3f))
            .drawBehind {
                drawRoundRect(color = Color.Black.copy(alpha = 0.5f), style = stroke)
            }
            .pointerInput(key1 = Unit) {
                detectDragGestures(onDrag = onDrag)
            }
            .transformable(state = state),
    ) {
        IconButton(
            modifier = Modifier.align(alignment = Alignment.TopStart),
            onClick = onHideClick
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Blue)
        }

        IconButton(
            modifier = Modifier.align(alignment = Alignment.TopEnd),
            onClick = onCloseClick
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Red)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            repeat(times = 9) { indexRow ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    repeat(times = 9) { indexColumn ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (sudoku.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(size = 8.dp)
                                        .background(color = Color.White.copy(alpha = 0.5f), shape = CircleShape)
                                )
                            } else {
                                when (val cell = sudoku[9 * indexRow + indexColumn]) {
                                    is Cell.Empty -> {
                                        Box(modifier = Modifier)
                                    }
                                    is Cell.Note -> {
                                        val text = buildString {
                                            val chunks = cell.possibleNumbers.chunked(size = 3)
                                            chunks.forEachIndexed { index, numbers ->
                                                append(" ")
                                                numbers.forEach { number ->
                                                    append(number)
                                                    append(" ")
                                                }
                                                if (index != chunks.size - 1) {
                                                    append("\n")
                                                }
                                            }
                                        }
                                        Text(
                                            text = text,
                                            color = Color.Cyan.copy(alpha = 0.55f),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    is Cell.Number -> {
                                        val color = if (cell.isStartNumber) Color.Magenta.copy(alpha = 0.55f) else Color.Blue.copy(alpha = 0.55f)
                                        Text(
                                            text = cell.number.toString(),
                                            color = color,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
            onClick = onScanClick
        ) {
            Text(text = "Scan", color = buttonTextColor)
        }

        Button(
            modifier = Modifier.align(alignment = Alignment.TopCenter),
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
            onClick = onCenterHorizontallyClick
        ) {
            Text(text = "Center", color = buttonTextColor)
        }

        Button(
            modifier = Modifier.align(alignment = Alignment.BottomStart),
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
            onClick = onMinusZoomClick
        ) {
            Text(text = "-", color = buttonTextColor)
        }
        Button(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
            onClick = onPlusZoomClick
        ) {
            Text(text = "+", color = buttonTextColor)
        }
    }
}
