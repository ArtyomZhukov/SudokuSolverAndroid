package com.zhukovartemvl.sudokusolver.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntOffset
import com.zhukovartemvl.sudokusolver.model.Cell
import com.zhukovartemvl.sudokusolver.model.TargetsParams
import com.zhukovartemvl.sudokusolver.preferences.SudokuPreferences
import com.zhukovartemvl.sudokusolver.state.OverlayState
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SudokuSolverOverlayComponent(
    val context: Context,
    private val overlayState: OverlayState,
    private val numbersTargetState: OverlayState,
    private val floatingButtonState: OverlayState,
    private val stopService: () -> Unit,
    private val startScanner: (gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) -> Unit,
    private val solveSudoku: () -> Unit,
    private val feelingLucky: (gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) -> Unit
) {
    private var sudoku by mutableStateOf(listOf<Cell>())

    private val preferences by lazy { SudokuPreferences(context = context) }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val screenWidthPx = context.resources.displayMetrics.widthPixels
    private val screenHeightPx = context.resources.displayMetrics.heightPixels

    private val gameFieldTargetOverlay: OverlayViewHolder = initClickTargetOverlay()
    private val numbersTargetsOverlay: OverlayViewHolder = initNumbersTargetsOverlay()
    private val floatingButtonOverlay: OverlayViewHolder = initFloatingButtonOverlay()

    private var isOverlayShowing = false

    private var isHidingMode = false

    private var gameFieldStartSize = 0
    private var numbersTargetsStartWidth = 0

    fun setSudokuNumbers(sudoku: List<Cell>) {
        this.sudoku = sudoku
    }

    private fun initClickTargetOverlay(): OverlayViewHolder {
        val (startXPosition, startYPosition) = preferences.gameFieldPosition

        val gameFieldSize = preferences.gameFieldSize
        gameFieldStartSize = if (gameFieldSize != 0) {
            gameFieldSize
        } else {
            min(context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels)
        }

        return OverlayViewHolder(
            params = WindowManager.LayoutParams(
                gameFieldStartSize,
                gameFieldStartSize,
                startXPosition,
                startYPosition,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ),
            context = context
        ).apply {
            view.setContent {
                FloatingWindowView(
                    sudoku = sudoku,
                    onDrag = { change, dragAmount: Offset ->
                        changePosition(
                            view = view,
                            params = params,
                            overlayState = overlayState,
                            change = change,
                            dragAmount = dragAmount
                        )
                    },
                    onScaleChange = { scaleChange ->
                        changeScale(view = view, params = params, scaleChange = scaleChange)
                    },
                    onScanClick = {
                        val (gameFieldParams, numbersTargetsParams) = getParams()
                        startScanner(gameFieldParams, numbersTargetsParams, getStatusBarHeight())
                    },
                    onHideClick = ::switchVisibility,
                    onCloseClick = stopService,
                    onCenterHorizontallyClick = {
                        centerView(view = view, params = params, overlayState = overlayState)
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
        preferences.gameFieldPosition = gameFieldTargetOverlay.params.x to gameFieldTargetOverlay.params.y
        preferences.gameFieldSize = gameFieldTargetOverlay.params.width
        preferences.numbersTargetsYPosition = numbersTargetsOverlay.params.y
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

    private fun initNumbersTargetsOverlay(): OverlayViewHolder {
        numbersTargetsStartWidth = context.resources.displayMetrics.widthPixels

        val yStartPos = if (preferences.numbersTargetsYPosition != 0) {
            preferences.numbersTargetsYPosition
        } else {
            context.resources.displayMetrics.heightPixels - 300
        }

        numbersTargetState.viewOffset = numbersTargetState.viewOffset.copy(y = yStartPos)

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
                FloatingTargetsView(
                    scanReady = sudoku.isNotEmpty(),
                    onSolveClick = solveSudoku,
                    onFeelingLuckyClick = {
                        val (gameFieldParams, numbersTargetsParams) = getParams()
                        feelingLucky(gameFieldParams, numbersTargetsParams, getStatusBarHeight())
                    },
                    onDrag = { change, dragAmount: Offset ->
                        changePosition(
                            view = view,
                            params = params,
                            overlayState = numbersTargetState,
                            change = change,
                            dragAmount = dragAmount
                        )
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

        floatingButtonState.viewOffset = floatingButtonState.viewOffset.copy(y = yStartPos)

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
                FloatingButtonView(
                    onShowClick = ::switchVisibility,
                    onDrag = { change, dragAmount: Offset ->
                        changePosition(
                            view = view,
                            params = params,
                            overlayState = floatingButtonState,
                            change = change,
                            dragAmount = dragAmount
                        )
                    }
                )
            }
        }
    }

    private fun changeScale(view: ComposeView, params: WindowManager.LayoutParams, scaleChange: Float, onlyWidth: Boolean = false) {
        params.width = min(gameFieldStartSize, max((params.width * scaleChange).toInt(), 700))
        if (!onlyWidth) {
            params.height = min(gameFieldStartSize, max((params.height * scaleChange).toInt(), 700))
        }
        windowManager.updateViewLayout(view, params)
    }

    private fun changePosition(
        view: ComposeView,
        params: WindowManager.LayoutParams,
        overlayState: OverlayState,
        change: PointerInputChange,
        dragAmount: Offset
    ) {
        change.consume()
        val dragAmountIntOffset = IntOffset(dragAmount.x.roundToInt(), dragAmount.y.roundToInt())
        val viewOffset = overlayState.viewOffset + dragAmountIntOffset

        val x = min(a = max(viewOffset.x, 0), b = screenWidthPx - params.width)
        val y = min(a = max(viewOffset.y, 0), b = screenHeightPx - params.height)

        overlayState.viewOffset = IntOffset(x, y)

        params.x = overlayState.viewOffset.x
        params.y = overlayState.viewOffset.y
        windowManager.updateViewLayout(view, params)
    }

    private fun centerView(
        view: ComposeView,
        params: WindowManager.LayoutParams,
        overlayState: OverlayState,
    ) {
        val viewOffset = overlayState.viewOffset
        val x = (screenWidthPx - params.width) / 2

        overlayState.viewOffset = IntOffset(x, viewOffset.y)
        params.x = overlayState.viewOffset.x
        windowManager.updateViewLayout(view, params)
    }

    fun showOverlays() {
        if (isOverlayShowing) {
            return
        }
        isOverlayShowing = true

        windowManager.addView(gameFieldTargetOverlay.view, gameFieldTargetOverlay.params)
        windowManager.addView(numbersTargetsOverlay.view, numbersTargetsOverlay.params)
    }

    fun hideOverlays() {
        try {
            windowManager.removeView(gameFieldTargetOverlay.view)
            windowManager.removeView(numbersTargetsOverlay.view)
        } catch (e: Exception) {

        }

        isOverlayShowing = false
    }

    private fun switchVisibility() {
        isHidingMode = !isHidingMode
        if (isHidingMode) {
            windowManager.removeView(gameFieldTargetOverlay.view)
            windowManager.removeView(numbersTargetsOverlay.view)

            windowManager.addView(floatingButtonOverlay.view, floatingButtonOverlay.params)
        } else {
            windowManager.removeView(floatingButtonOverlay.view)

            windowManager.addView(gameFieldTargetOverlay.view, gameFieldTargetOverlay.params)
            windowManager.addView(numbersTargetsOverlay.view, numbersTargetsOverlay.params)
        }
    }

    fun removeOverlays() {
        windowManager.removeView(gameFieldTargetOverlay.view)
        windowManager.removeView(numbersTargetsOverlay.view)
        windowManager.removeView(floatingButtonOverlay.view)

        overlayState.viewOffset = IntOffset.Zero
        numbersTargetState.viewOffset = IntOffset.Zero
        gameFieldTargetOverlay.params.x = 0
        gameFieldTargetOverlay.params.y = 0

        gameFieldTargetOverlay.view.disposeComposition()
        numbersTargetsOverlay.view.disposeComposition()
        floatingButtonOverlay.view.disposeComposition()

        isOverlayShowing = false
        isHidingMode = false
    }
}
