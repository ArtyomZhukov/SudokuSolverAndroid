package com.zhukovartemvl.sudokusolver.service.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntOffset
import com.zhukovartemvl.sudokusolver.domain.model.TargetsParams
import com.zhukovartemvl.sudokusolver.service.ServiceOverlayState
import com.zhukovartemvl.sudokusolver.service.SudokuSolverServiceState
import com.zhukovartemvl.sudokusolver.ui.service_views.FloatingButtonView
import com.zhukovartemvl.sudokusolver.ui.service_views.FloatingTargetsView
import com.zhukovartemvl.sudokusolver.ui.service_views.FloatingWindowView
import com.zhukovartemvl.sudokusolver.ui.service_views.HappyCatView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SudokuSolverOverlayComponent(
    val context: Context,
    coroutineScope: CoroutineScope,
    private val state: StateFlow<SudokuSolverServiceState>,
    private val onFeelingLuckyClick: (gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) -> Unit,
    private val onStartScannerClick: (gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) -> Unit,
    private val onSolveSudokuClick: () -> Unit,
    private val onStopServiceClick: () -> Unit,
    private val onHideClick: () -> Unit,
    private val onShowClick: () -> Unit,
    private val onFloatingWindowPositionChange: (IntOffset) -> Unit,
    private val onNumbersTargetsPositionChange: (IntOffset) -> Unit,
    private val onFloatingButtonPositionChange: (IntOffset) -> Unit,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val screenWidthPx = context.resources.displayMetrics.widthPixels
    private val screenHeightPx = context.resources.displayMetrics.heightPixels
    private val smallerScreenSizePx = min(screenWidthPx, screenHeightPx)

    private val gameFieldTargetOverlay: OverlayViewHolder = initClickTargetOverlay()
    private val numbersTargetsOverlay: OverlayViewHolder = initNumbersTargetsOverlay()
    private val floatingButtonOverlay: OverlayViewHolder = initFloatingButtonOverlay()
    private val catOverlay: OverlayViewHolder = initCatOverlay()

    init {
        coroutineScope.launch {
            state.collect { overlayState ->
                delay(50)
                when (overlayState.overlayState) {
                    ServiceOverlayState.Init -> Unit
                    ServiceOverlayState.Default -> {
                        windowManager.tryToAddView(gameFieldTargetOverlay.view, gameFieldTargetOverlay.params)
                        windowManager.tryToAddView(numbersTargetsOverlay.view, numbersTargetsOverlay.params)
                        windowManager.tryToRemoveView(floatingButtonOverlay.view)
                        windowManager.tryToRemoveView(catOverlay.view)
                    }
                    ServiceOverlayState.FloatingButton -> {
                        windowManager.tryToRemoveView(gameFieldTargetOverlay.view)
                        windowManager.tryToRemoveView(numbersTargetsOverlay.view)
                        windowManager.tryToRemoveView(catOverlay.view)
                        windowManager.tryToAddView(floatingButtonOverlay.view, floatingButtonOverlay.params)
                    }
                    ServiceOverlayState.MakingScreenshot -> {
                        windowManager.tryToRemoveView(gameFieldTargetOverlay.view)
                        windowManager.tryToRemoveView(numbersTargetsOverlay.view)
                        windowManager.tryToAddView(catOverlay.view, catOverlay.params)
                    }
                    ServiceOverlayState.SudokuRecognizing -> Unit
                    ServiceOverlayState.SudokuSolving -> {
                        windowManager.tryToRemoveView(gameFieldTargetOverlay.view)
                        windowManager.tryToRemoveView(numbersTargetsOverlay.view)
                        windowManager.tryToAddView(catOverlay.view, catOverlay.params)
                    }
                    ServiceOverlayState.SudokuClicking -> Unit
                }
            }
        }
    }

    private fun initClickTargetOverlay(): OverlayViewHolder {
        val startOffset = state.value.gameFieldOverlayOffset

        val gameFieldStartSize = if (state.value.initGameFieldSize != 0) {
            state.value.initGameFieldSize
        } else {
            min(context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels)
        }

        return OverlayViewHolder(
            params = WindowManager.LayoutParams(
                gameFieldStartSize,
                gameFieldStartSize,
                startOffset.x,
                startOffset.y,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ),
            context = context
        ).apply {
            view.setContent {
                val viewState by state.collectAsState()

                FloatingWindowView(
                    sudoku = viewState.sudokuNumbers,
                    onDrag = { change, dragAmount: Offset ->
                        val newPosition = changePosition(
                            view = view,
                            params = params,
                            viewOffset = state.value.gameFieldOverlayOffset,
                            dragAmount = dragAmount
                        )
                        onFloatingWindowPositionChange(newPosition)
                        change.consume()
                    },
                    onScaleChange = { scaleChange ->
                        changeScale(view = view, params = params, scaleChange = scaleChange)
                    },
                    onScanClick = {
                        val (gameFieldParams, numbersTargetsParams) = getParams()
                        onStartScannerClick(gameFieldParams, numbersTargetsParams, getStatusBarHeight())
                    },
                    onHideClick = onHideClick,
                    onCloseClick = onStopServiceClick,
                    onCenterHorizontallyClick = {
                        val newPosition = centerView(view = view, params = params, viewOffset = state.value.gameFieldOverlayOffset)
                        onFloatingWindowPositionChange(newPosition)
                    },
                    onMinusZoomClick = {
                        changeScale(view = view, params = params, scaleChange = 0.995f)
                    },
                    onPlusZoomClick = {
                        changeScale(view = view, params = params, scaleChange = 1.005f)
                    }
                )
            }
        }
    }

    private fun initNumbersTargetsOverlay(): OverlayViewHolder {
        val numbersTargetsStartWidth = context.resources.displayMetrics.widthPixels

        val numbersTargetsYPosition = state.value.numbersTargetOffset.y

        val yStartPos = if (numbersTargetsYPosition != 0) {
            numbersTargetsYPosition
        } else {
            context.resources.displayMetrics.heightPixels - 300
        }

        onNumbersTargetsPositionChange(IntOffset(x = 0, y = yStartPos))

        return OverlayViewHolder(
            params = WindowManager.LayoutParams(
                numbersTargetsStartWidth,
                200,
                0,
                yStartPos,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ),
            context = context
        ).apply {
            view.setContent {
                val viewState by state.collectAsState()

                FloatingTargetsView(
                    sudokuNumbersScanned = viewState.sudokuNumbers.isNotEmpty(),
                    onSolveClick = onSolveSudokuClick,
                    onFeelingLuckyClick = {
                        val (gameFieldParams, numbersTargetsParams) = getParams()
                        onFeelingLuckyClick(gameFieldParams, numbersTargetsParams, getStatusBarHeight())
                    },
                    onDrag = { change, dragAmount: Offset ->
                        val newPosition = changePosition(
                            view = view,
                            params = params,
                            viewOffset = viewState.numbersTargetOffset,
                            dragAmount = dragAmount
                        )
                        onNumbersTargetsPositionChange(newPosition)
                        change.consume()
                    },
                    onScaleChange = { scaleChange ->
                        changeScale(view = view, params = params, scaleChange = scaleChange, onlyWidth = true)
                    }
                )
            }
        }
    }

    private fun initFloatingButtonOverlay(): OverlayViewHolder {
        val yStartPos = context.resources.displayMetrics.heightPixels / 2

        onFloatingButtonPositionChange(IntOffset(x = 0, y = yStartPos))

        return OverlayViewHolder(
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,
                yStartPos,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ),
            context = context
        ).apply {
            view.setContent {
                val viewState by state.collectAsState()

                FloatingButtonView(
                    onShowClick = onShowClick,
                    onDrag = { change, dragAmount: Offset ->
                        val newPosition = changePosition(
                            view = view,
                            params = params,
                            viewOffset = viewState.floatingButtonOffset,
                            dragAmount = dragAmount
                        )
                        onFloatingButtonPositionChange(newPosition)
                        change.consume()
                    }
                )
            }
        }
    }

    private fun initCatOverlay(): OverlayViewHolder {
        val gameFieldSize = state.value.initGameFieldSize
        val startOffset = state.value.gameFieldOverlayOffset
        val numbersTargetsYPosition = state.value.numbersTargetOffset.y

        val size = numbersTargetsYPosition - startOffset.y - gameFieldSize

        val xStartPos = (context.resources.displayMetrics.widthPixels / 2) - (size / 2)
        val yStartPos = startOffset.y + gameFieldSize

        return OverlayViewHolder(
            params = WindowManager.LayoutParams(
                size,
                size,
                xStartPos,
                yStartPos,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ),
            context = context
        ).apply {
            view.setContent {
                val viewState by state.collectAsState()
                Box {
                    HappyCatView(modifier = Modifier.fillMaxSize())
                    val text = when (viewState.overlayState) {
                        ServiceOverlayState.MakingScreenshot -> "making screenshot"
                        ServiceOverlayState.SudokuRecognizing -> "recognizing"
                        ServiceOverlayState.SudokuSolving -> "solving"
                        ServiceOverlayState.SudokuClicking -> "clicking"
                        else -> ""
                    }
                    Text(
                        modifier = Modifier.align(alignment = Alignment.BottomCenter),
                        text = text
                    )
                }
            }
        }
    }

    private fun getParams(): Pair<TargetsParams, TargetsParams> {
        val gameFieldParams = TargetsParams(
            width = gameFieldTargetOverlay.params.width,
            height = gameFieldTargetOverlay.params.height,
            xPosition = gameFieldTargetOverlay.params.x,
            yPosition = gameFieldTargetOverlay.params.y
        )
        val numbersTargetsParams = TargetsParams(
            width = numbersTargetsOverlay.params.width,
            height = numbersTargetsOverlay.params.height,
            xPosition = numbersTargetsOverlay.params.x,
            yPosition = numbersTargetsOverlay.params.y
        )
        return gameFieldParams to numbersTargetsParams
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun changeScale(view: ComposeView, params: WindowManager.LayoutParams, scaleChange: Float, onlyWidth: Boolean = false) {
        params.width = min(smallerScreenSizePx, max((params.width * scaleChange).toInt(), 700))
        if (!onlyWidth) {
            params.height = min(smallerScreenSizePx, max((params.height * scaleChange).toInt(), 700))
        }
        windowManager.updateViewLayout(view, params)
    }

    private fun changePosition(
        view: ComposeView,
        params: WindowManager.LayoutParams,
        viewOffset: IntOffset,
        dragAmount: Offset
    ): IntOffset {
        val dragAmountIntOffset = IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt())
        val newViewOffset = viewOffset + dragAmountIntOffset

        val x = min(a = max(newViewOffset.x, 0), b = screenWidthPx - params.width)
        val y = min(a = max(newViewOffset.y, 0), b = screenHeightPx - params.height)

        params.x = x
        params.y = y
        windowManager.updateViewLayout(view, params)
        return IntOffset(x, y)
    }

    private fun centerView(
        view: ComposeView,
        params: WindowManager.LayoutParams,
        viewOffset: IntOffset,
    ): IntOffset {
        val x = (screenWidthPx - params.width) / 2
        params.x = x
        windowManager.updateViewLayout(view, params)
        return IntOffset(x, viewOffset.y)
    }

    fun onDestroy() {
        gameFieldTargetOverlay.close()
        numbersTargetsOverlay.close()
        floatingButtonOverlay.close()
        catOverlay.close()

        windowManager.tryToRemoveView(gameFieldTargetOverlay.view)
        windowManager.tryToRemoveView(numbersTargetsOverlay.view)
        windowManager.tryToRemoveView(floatingButtonOverlay.view)

        gameFieldTargetOverlay.view.disposeComposition()
        numbersTargetsOverlay.view.disposeComposition()
        floatingButtonOverlay.view.disposeComposition()
    }

    private fun WindowManager.tryToAddView(view: View, layoutParams: ViewGroup.LayoutParams) {
        try {
            addView(view, layoutParams)
        } catch (_: Exception) {
        }
    }

    private fun WindowManager.tryToRemoveView(view: View) {
        try {
            removeView(view)
        } catch (_: Exception) {
        }
    }
}
