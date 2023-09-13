package com.zhukovartemvl.sudokusolver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import androidx.compose.ui.unit.IntOffset
import androidx.core.app.NotificationCompat
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_EXIT
import com.zhukovartemvl.sudokusolver.INTENT_COMMAND_START
import com.zhukovartemvl.sudokusolver.REQUEST_CODE_EXIT_COUNTDOWN
import com.zhukovartemvl.sudokusolver.SUDOKU_SOLVER_SERVICE_NOTIFICATION_CHANNEL
import com.zhukovartemvl.sudokusolver.SUDOKU_SOLVER_SERVICE_NOTIFICATION_ID
import com.zhukovartemvl.sudokusolver.domain.SudokuSolverInteractor
import com.zhukovartemvl.sudokusolver.domain.model.TargetsParams
import com.zhukovartemvl.sudokusolver.preferences.SudokuPreferences
import com.zhukovartemvl.sudokusolver.service.component.SudokuSolverOverlayComponent
import com.zhukovartemvl.sudokusolver.service.receiver.ExitServiceReceiver
import com.zhukovartemvl.sudokusolver.tools.ImageCVScanner
import com.zhukovartemvl.sudokusolver.tools.ScreenshotMaker
import com.zhukovartemvl.sudokusolver.tools.SudokuSolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SudokuSolverOverlayService : Service() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val screenshotMaker: ScreenshotMaker by inject()
    private val imageCVScanner: ImageCVScanner by inject()

    private val preferences: SudokuPreferences by inject()

    private val serviceState: MutableStateFlow<SudokuSolverServiceState> = MutableStateFlow(SudokuSolverServiceState())

    private var overlayComponent: SudokuSolverOverlayComponent? = null

    private val sudokuSolverInteractor = SudokuSolverInteractor()

    private var serviceLooper: Looper? = null
    private var serviceHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        HandlerThread("Service", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = Handler(looper)
        }
        initOverlayPositions()
        overlayComponent = SudokuSolverOverlayComponent(
            context = this,
            coroutineScope = coroutineScope,
            state = serviceState,
            onFeelingLuckyClick = ::feelingLucky,
            onSolveSudokuClick = ::solveSudoku,
            onStartScannerClick = ::startScanner,
            onStopServiceClick = ::serviceExit,
            onHideClick = { serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.FloatingButton) },
            onShowClick = { serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.Default) },
            onFloatingWindowPositionChange = { offset -> serviceState.value = serviceState.value.copy(gameFieldOverlayOffset = offset) },
            onNumbersTargetsPositionChange = { offset -> serviceState.value = serviceState.value.copy(numbersTargetOffset = offset) },
            onFloatingButtonPositionChange = { offset -> serviceState.value = serviceState.value.copy(floatingButtonOffset = offset) },
        )
    }

    private fun startScanner(gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) {
        startScanner(gameFieldParams = gameFieldParams, numbersTargetsParams = numbersTargetsParams, statusBarHeight = statusBarHeight) {
            serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.Default)
        }
    }

    private fun feelingLucky(gameFieldParams: TargetsParams, numbersTargetsParams: TargetsParams, statusBarHeight: Int) {
        startScanner(gameFieldParams = gameFieldParams, numbersTargetsParams = numbersTargetsParams, statusBarHeight = statusBarHeight) {
            solveSudoku()
        }
    }

    private fun initOverlayPositions() {
        val (startXPosition, startYPosition) = preferences.gameFieldPosition
        serviceState.value = serviceState.value.copy(
            initGameFieldSize = preferences.gameFieldSize,
            gameFieldOverlayOffset = IntOffset(startXPosition, startYPosition),
            numbersTargetOffset = IntOffset(x = 0, y = preferences.numbersTargetsYPosition)
        )
    }

    override fun onStartCommand(intentOrNull: Intent?, flags: Int, startId: Int): Int {
        intentOrNull?.getStringExtra(INTENT_COMMAND)?.let { command ->
            when (command) {
                INTENT_COMMAND_EXIT -> {
                    serviceExit()
                    return START_NOT_STICKY
                }
                INTENT_COMMAND_START -> {
                    serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.Default)
                }
                else -> Unit
            }
        }
        val notification = buildNotification()
        startForeground(SUDOKU_SOLVER_SERVICE_NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onBind(intent: Intent?) = Binder()

    private fun startScanner(
        gameFieldParams: TargetsParams,
        numbersTargetsParams: TargetsParams,
        statusBarHeight: Int,
        onResult: suspend () -> Unit
    ) {
        serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.MakingScreenshot)

        preferences.gameFieldPosition = gameFieldParams.xPosition to gameFieldParams.yPosition
        preferences.gameFieldSize = gameFieldParams.width
        preferences.numbersTargetsYPosition = numbersTargetsParams.yPosition

        coroutineScope.launch {
            val gameFieldTargets = sudokuSolverInteractor.createGameFieldTargets(
                gameFieldParams = gameFieldParams,
                statusBarHeight = statusBarHeight
            )
            val numbersTargets = sudokuSolverInteractor.createNumbersTargets(
                numbersTargetsParams = numbersTargetsParams,
                statusBarHeight = statusBarHeight
            )
            delay(50)
            serviceState.value = serviceState.value.copy(
                gameFieldParams = gameFieldParams,
                gameFieldTargets = gameFieldTargets,
                numbersTargets = numbersTargets
            )
            delay(50)
            if (serviceHandler != null) {
                screenshotMaker.makeScreenShot(
                    context = applicationContext,
                    serviceHandler = serviceHandler ?: throw Exception("serviceHandler must be not null!"),
                    onResult = { mat ->
                        coroutineScope.launch {
                            serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.SudokuRecognizing)

                            val sudokuNumbers = imageCVScanner.scanMat(
                                context = this@SudokuSolverOverlayService,
                                gameFieldMat = mat,
                                gameFieldParams = gameFieldParams
                            )
                            serviceState.value = serviceState.value.copy(sudokuNumbers = sudokuNumbers)
                            mat.release()
                            onResult()
                        }
                    }
                )
            }
        }
    }

    private fun solveSudoku() {
        coroutineScope.launch {
            delay(10)
            serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.SudokuSolving)

            withContext(Dispatchers.Default) {
                val solvedSudoku = SudokuSolver.solve(sudoku = serviceState.value.sudokuNumbers)
                serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.SudokuClicking)
                delay(100)
                val solvedSudokuCells = sudokuSolverInteractor.getCellsToClick(
                    initialSudoku = serviceState.value.sudokuNumbers,
                    solvedSudoku = solvedSudoku
                )
                sudokuSolverInteractor.startAutoClicker(
                    sudoku = solvedSudokuCells,
                    gameFieldTargets = serviceState.value.gameFieldTargets,
                    numbersTargets = serviceState.value.numbersTargets,
                    clickOnTarget = { xPosition, yPosition ->
                        SudokuSolverAutoClickerService.instance?.makeClickOnPosition(xPos = xPosition, yPos = yPosition)
                    },
                    onComplete = {
                        serviceState.value = serviceState.value.copy(overlayState = ServiceOverlayState.Default, sudokuNumbers = listOf())
                    }
                )
            }
        }
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannel(
            SUDOKU_SOLVER_SERVICE_NOTIFICATION_CHANNEL,
            "Sudoku Solver",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val exitIntent = Intent(applicationContext, ExitServiceReceiver::class.java)
        val exitPendingIntent = PendingIntent.getBroadcast(
            applicationContext, REQUEST_CODE_EXIT_COUNTDOWN, exitIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SUDOKU_SOLVER_SERVICE_NOTIFICATION_CHANNEL)
            .setContentTitle("Sudoku Solver")
            .setSmallIcon(androidx.appcompat.R.drawable.abc_dialog_material_background)
            .setContentText("AutoClicker")
            .addAction(0, "Exit", exitPendingIntent)
            .build()
    }

    private fun serviceExit() {
        overlayComponent?.onDestroy()
        stopSelf()
    }
}
