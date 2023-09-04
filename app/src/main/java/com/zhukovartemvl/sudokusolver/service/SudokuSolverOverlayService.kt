package com.zhukovartemvl.sudokusolver.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
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

class SudokuSolverOverlayService : AccessibilityService() {

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var overlayComponent: SudokuSolverOverlayComponent
    private val overlayState = OverlayState()
    private val numbersTargetState = OverlayState()

    private val sudokuSolverInteractor = SudokuSolverInteractor()

    private var mediaProjectionIntent: Intent? = AppActivity.mediaProjectionIntent?.clone() as Intent

    override fun onCreate() {
        super.onCreate()
        overlayComponent = SudokuSolverOverlayComponent(
            context = this,
            overlayState = overlayState,
            numbersTargetState = numbersTargetState,
            stopService = { serviceExit(overlayComponent = overlayComponent) },
            startSolver = { gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams ->
                coroutineScope.launch {
                    sudokuSolverInteractor.setTargets(gameFieldParams = gameFieldParams, numbersTargetsParams = numbersTargetsParams)

                    overlayComponent.hideOverlays()

                    delay(100)
                    val intent = mediaProjectionIntent
                    if (intent != null) {
                        ScreenshotMaker.makeScreenShot(
                            context = applicationContext,
                            mediaProjectionIntent = intent,
                            onResult = { mat ->
                                val sudoku = sudokuSolverInteractor.scanScreenshot(context = this@SudokuSolverOverlayService, mat = mat)
                                overlayComponent.setSudokuNumbers(sudoku = sudoku)
                                overlayComponent.showOverlays()
                            },
                            onFailure = {
                                overlayComponent.showOverlays()
                            }
                        )
                    }
                }
            }
        )
    }

    override fun onStartCommand(intentOrNull: Intent?, flags: Int, startId: Int): Int {
        intentOrNull?.let { intent ->
            when (intent.getStringExtra(INTENT_COMMAND)) {
                INTENT_COMMAND_EXIT -> {
                    serviceExit(overlayComponent = overlayComponent)
                    return START_NOT_STICKY
                }
                INTENT_COMMAND_START -> {
                    // mediaProjectionIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT)
                    // if (mediaProjectionIntent != null) {
                    overlayComponent.showOverlays()
                    // } else {
                    //     serviceExit(overlayComponent = overlayComponent)
                    // }
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
