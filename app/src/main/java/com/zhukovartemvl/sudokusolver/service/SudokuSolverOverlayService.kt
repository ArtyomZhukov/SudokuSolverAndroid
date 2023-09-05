package com.zhukovartemvl.sudokusolver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.zhukovartemvl.sudokusolver.AppActivity
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_EXIT
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_START
import com.zhukovartemvl.sudokusolver.REQUEST_CODE_EXIT_COUNTDOWN
import com.zhukovartemvl.sudokusolver.SUDOKU_SOLVER_SERVICE_NOTIFICATION_CHANNEL
import com.zhukovartemvl.sudokusolver.SUDOKU_SOLVER_SERVICE_NOTIFICATION_ID
import com.zhukovartemvl.sudokusolver.component.SudokuSolverOverlayComponent
import com.zhukovartemvl.sudokusolver.interactor.SudokuSolverInteractor
import com.zhukovartemvl.sudokusolver.model.TargetsParams
import com.zhukovartemvl.sudokusolver.service.receiver.ExitReceiver
import com.zhukovartemvl.sudokusolver.state.OverlayState
import com.zhukovartemvl.sudokusolver.util.ScreenshotMaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SudokuSolverOverlayService : AccessibilityService() {

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var overlayComponent: SudokuSolverOverlayComponent
    private val overlayState = OverlayState()
    private val numbersTargetState = OverlayState()

    private val sudokuSolverInteractor = SudokuSolverInteractor()

    private val mediaProjectionIntent: Intent?
        get() = AppActivity.mediaProjectionIntent?.clone() as? Intent

    override fun onCreate() {
        super.onCreate()
        overlayComponent = SudokuSolverOverlayComponent(
            context = this,
            overlayState = overlayState,
            numbersTargetState = numbersTargetState,
            stopService = { serviceExit(overlayComponent = overlayComponent) },
            startScanner = { gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int ->
                coroutineScope.launch {
                    sudokuSolverInteractor.setTargets(
                        gameFieldParams = gameFieldParams,
                        numbersTargetsParams = numbersTargetsParams,
                        statusBarHeight = statusBarHeight
                    )

                    delay(100)
                    overlayComponent.hideOverlays()

                    delay(100)
                    val intent = mediaProjectionIntent
                    if (intent != null) {
                        ScreenshotMaker.makeScreenShot(
                            context = applicationContext,
                            mediaProjectionIntent = intent,
                            onResult = { mat ->
                                coroutineScope.launch {
                                    sudokuSolverInteractor.scanScreenshot(context = this@SudokuSolverOverlayService, mat = mat)
                                    mat.release()

                                    overlayComponent.setSudokuNumbers(sudoku = sudokuSolverInteractor.sudokuCells)
                                    delay(50)
                                    overlayComponent.showOverlays()
                                }
                            },
                            onFailure = {
                                overlayComponent.showOverlays()
                            }
                        )
                    }
                }
            },
            solveSudoku = {
                coroutineScope.launch {
                    delay(100)
                    overlayComponent.hideOverlays()
                    withContext(Dispatchers.Default) {
                        val sudoku = sudokuSolverInteractor.solveSudoku()
                        delay(50)
                        sudokuSolverInteractor.startAutoClicker(
                            sudoku = sudoku,
                            clickOnTarget = ::makeClickOnPosition
                        ) {
                            withContext(Dispatchers.Main) {
                                overlayComponent.setSudokuNumbers(sudoku = listOf())
                                delay(50)
                                overlayComponent.showOverlays()
                            }
                        }
                    }
                }
            }
        )
    }

    private fun makeClickOnPosition(xPos: Int, yPos: Int) {
        val path = Path()
        path.moveTo(xPos.toFloat(), yPos.toFloat())
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(path, 0, 1)
        )
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onStartCommand(intentOrNull: Intent?, flags: Int, startId: Int): Int {
        intentOrNull?.let { intent ->
            when (intent.getStringExtra(INTENT_COMMAND)) {
                INTENT_COMMAND_EXIT -> {
                    serviceExit(overlayComponent = overlayComponent)
                    return START_NOT_STICKY
                }
                INTENT_COMMAND_START -> {
                    overlayComponent.showOverlays()
                }
                else -> Unit
            }
        }

        startForeground(SUDOKU_SOLVER_SERVICE_NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() = Unit

    private fun buildNotification(): Notification {
        val channel = NotificationChannel(
            SUDOKU_SOLVER_SERVICE_NOTIFICATION_CHANNEL,
            "Sudoku Solver",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        val exitIntent = Intent(applicationContext, ExitReceiver::class.java)
        val exitPendingIntent = PendingIntent.getBroadcast(
            applicationContext, REQUEST_CODE_EXIT_COUNTDOWN, exitIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SUDOKU_SOLVER_SERVICE_NOTIFICATION_CHANNEL)
            .setContentTitle("Sudoku Solver")
            .setContentText("AutoClicker")
            .addAction(0, "Exit", exitPendingIntent)
            .build()
    }
}
